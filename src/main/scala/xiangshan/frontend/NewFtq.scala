/***************************************************************************************
* Copyright (c) 2020-2021 Institute of Computing Technology, Chinese Academy of Sciences
* Copyright (c) 2020-2021 Peng Cheng Laboratory
*
* XiangShan is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

package xiangshan.frontend

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import chisel3.util._
import utils._
import xiangshan._
import xiangshan.frontend.icache._
import xiangshan.backend.CtrlToFtqIO
import xiangshan.backend.decode.ImmUnion

class FtqPtr(implicit p: Parameters) extends CircularQueuePtr[FtqPtr](
  p => p(XSCoreParamsKey).FtqSize
){
}

object FtqPtr {
  def apply(f: Bool, v: UInt)(implicit p: Parameters): FtqPtr = {
    val ptr = Wire(new FtqPtr)
    ptr.flag := f
    ptr.value := v
    ptr
  }
  def inverse(ptr: FtqPtr)(implicit p: Parameters): FtqPtr = {
    apply(!ptr.flag, ptr.value)
  }
}

class FtqNRSRAM[T <: Data](gen: T, numRead: Int)(implicit p: Parameters) extends XSModule {

  val io = IO(new Bundle() {
    val raddr = Input(Vec(numRead, UInt(log2Up(FtqSize).W)))
    val ren = Input(Vec(numRead, Bool()))
    val rdata = Output(Vec(numRead, gen))
    val waddr = Input(UInt(log2Up(FtqSize).W))
    val wen = Input(Bool())
    val wdata = Input(gen)
  })

  for(i <- 0 until numRead){
    val sram = Module(new SRAMTemplate(gen, FtqSize))
    sram.io.r.req.valid := io.ren(i)
    sram.io.r.req.bits.setIdx := io.raddr(i)
    io.rdata(i) := sram.io.r.resp.data(0)
    sram.io.w.req.valid := io.wen
    sram.io.w.req.bits.setIdx := io.waddr
    sram.io.w.req.bits.data := VecInit(io.wdata)
  }

}

class Ftq_RF_Components(implicit p: Parameters) extends XSBundle with BPUUtils with HasBPUConst {
  val startAddr = UInt(VAddrBits.W)
  val nextLineAddr = UInt(VAddrBits.W)
  val isNextMask = Vec(PredictWidth, Bool())
  val fallThruError = Bool()
  val isDouble = Bool()
  // val is_loop = Bool()
  // val carry = Bool()
  def getPc(offset: UInt): UInt = {
    def getHigher(pc: UInt) = pc(VAddrBits-1, log2Ceil(PredictWidth)+instOffsetBits+1)
    def getOffset(pc: UInt) = pc(log2Ceil(PredictWidth)+instOffsetBits, instOffsetBits)

    val real_off = Mux(isDouble, Cat(0.U.asTypeOf(UInt(1.W)), offset(log2Ceil(PredictWidth) - 2, 0)), offset)

    Cat(getHigher(Mux(isNextMask(real_off) && startAddr(log2Ceil(PredictWidth)+instOffsetBits), nextLineAddr, startAddr)),
        getOffset(startAddr)+real_off, 0.U(instOffsetBits.W))
  }
  def fromBranchPrediction(resp: BranchPredictionBundle) = {
    def carryPos(addr: UInt) = addr(instOffsetBits+log2Ceil(PredictWidth)+1)
    this.startAddr := resp.pc(dupForFtq)
    this.nextLineAddr := resp.pc(dupForFtq) + (FetchWidth * 4 * 2).U // may be broken on other configs
    this.isNextMask := VecInit((0 until PredictWidth).map(i =>
      (resp.pc(dupForFtq)(log2Ceil(PredictWidth), 1) +& i.U)(log2Ceil(PredictWidth)).asBool()
    ))
    this.fallThruError := resp.fallThruError(dupForFtq)
    this.isDouble := resp.isDouble
    // this.is_loop := resp.full_pred(dupForFtq).is_loop
    this
  }
  override def toPrintable: Printable = {
    p"startAddr:${Hexadecimal(startAddr)}"
  }
}

class xsLPpredInfo(implicit p: Parameters) extends XSBundle with LoopPredictorParams {
  val isConfExitLoop = Output(Bool())
  val target         = Output(UInt(VAddrBits.W))
  val isInterNumGT2  = Output(Bool())
  val remainIterNum  = Output(UInt(cntBits.W))
  val isConf         = Output(Bool())
}



class LastStageInfo(implicit p: Parameters) extends XSBundle with HasBPUConst {
  val last_stage_meta = UInt(MaxMetaLength.W)
  val last_stage_spec_info = new SpeculativeInfo
  val last_stage_ftb_entry = new FTBEntry
  val ftqIdx = new FtqPtr
}


class Ftq_pd_Entry(implicit p: Parameters) extends XSBundle {
  val brMask = Vec(PredictWidth, Bool())
  val jmpInfo = ValidUndirectioned(Vec(3, Bool()))
  val jmpOffset = UInt(log2Ceil(PredictWidth).W)
  val jalTarget = UInt(VAddrBits.W)
  val rvcMask = Vec(PredictWidth, Bool())
  def hasJal  = jmpInfo.valid && !jmpInfo.bits(0)
  def hasJalr = jmpInfo.valid && jmpInfo.bits(0)
  def hasCall = jmpInfo.valid && jmpInfo.bits(1)
  def hasRet  = jmpInfo.valid && jmpInfo.bits(2)

  def fromPdWb(pdWb: PredecodeWritebackBundle) = {
    val pds = pdWb.pd
    this.brMask := VecInit(pds.map(pd => pd.isBr && pd.valid))
    this.jmpInfo.valid := VecInit(pds.map(pd => (pd.isJal || pd.isJalr) && pd.valid)).asUInt.orR
    this.jmpInfo.bits := ParallelPriorityMux(pds.map(pd => (pd.isJal || pd.isJalr) && pd.valid),
                                             pds.map(pd => VecInit(pd.isJalr, pd.isCall, pd.isRet)))
    this.jmpOffset := ParallelPriorityEncoder(pds.map(pd => (pd.isJal || pd.isJalr) && pd.valid))
    this.rvcMask := VecInit(pds.map(pd => pd.isRVC))
    this.jalTarget := pdWb.jalTarget
  }

  def toPd(offset: UInt) = {
    require(offset.getWidth == log2Ceil(PredictWidth))
    val pd = Wire(new PreDecodeInfo)
    pd.valid := true.B
    pd.isRVC := rvcMask(offset)
    val isBr = brMask(offset)
    val isJalr = offset === jmpOffset && jmpInfo.valid && jmpInfo.bits(0)
    pd.brType := Cat(offset === jmpOffset && jmpInfo.valid, isJalr || isBr)
    pd.isCall := offset === jmpOffset && jmpInfo.valid && jmpInfo.bits(1)
    pd.isRet  := offset === jmpOffset && jmpInfo.valid && jmpInfo.bits(2)
    pd
  }
}

class Ftq_Redirect_SRAMEntry(implicit p: Parameters) extends SpeculativeInfo {}

class Ftq_1R_SRAMEntry(implicit p: Parameters) extends XSBundle with HasBPUConst {
  val meta = UInt(MaxMetaLength.W)
}

class Ftq_Pred_Info(implicit p: Parameters) extends XSBundle {
  val target = UInt(VAddrBits.W)
  val cfiIndex = ValidUndirectioned(UInt(log2Ceil(PredictWidth).W))
}


class FtqRead[T <: Data](private val gen: T)(implicit p: Parameters) extends XSBundle {
  val ptr = Output(new FtqPtr)
  val offset = Output(UInt(log2Ceil(PredictWidth).W))
  val data = Input(gen)
  def apply(ptr: FtqPtr, offset: UInt) = {
    this.ptr := ptr
    this.offset := offset
    this.data
  }
}


class FtqToBpuIO(implicit p: Parameters) extends XSBundle {
  val redirect = Valid(new BranchPredictionRedirect)
  val update = Valid(new BranchPredictionUpdate)
  val enq_ptr = Output(new FtqPtr)
}

class FtqToIfuIO(implicit p: Parameters) extends XSBundle with HasCircularQueuePtrHelper {
  val req = Decoupled(new FetchRequestBundle)
  val redirect = Valid(new Redirect)
  val flushFromBpu = new Bundle {
    // when ifu pipeline is not stalled,
    // a packet from bpu s3 can reach f1 at most
    val s2 = Valid(new FtqPtr)
    val s3 = Valid(new FtqPtr)
    def shouldFlushBy(src: Valid[FtqPtr], idx_to_flush: FtqPtr) = {
      src.valid && !isAfter(src.bits, idx_to_flush)
    }
    def shouldFlushByStage2(idx: FtqPtr) = shouldFlushBy(s2, idx)
    def shouldFlushByStage3(idx: FtqPtr) = shouldFlushBy(s3, idx)
  }
}

class FtqToICacheIO(implicit p: Parameters) extends XSBundle with HasCircularQueuePtrHelper {
  //NOTE: req.bits must be prepare in T cycle
  // while req.valid is set true in T + 1 cycle
  val req = Decoupled(new FtqToICacheRequestBundle)
}

trait HasBackendRedirectInfo extends HasXSParameter {
  def numRedirectPcRead = exuParameters.JmpCnt + exuParameters.AluCnt + 1
  def isLoadReplay(r: Valid[Redirect]) = r.bits.flushItself()
}

class FtqToCtrlIO(implicit p: Parameters) extends XSBundle with HasBackendRedirectInfo {
  // write to backend pc mem
  val pc_mem_wen = Output(Bool())
  val pc_mem_waddr = Output(UInt(log2Ceil(FtqSize).W))
  val pc_mem_wdata = Output(new Ftq_RF_Components)
  // newest target
  val newest_entry_target = Output(UInt(VAddrBits.W))
  val newest_entry_ptr = Output(new FtqPtr)
}


class FTBEntryGen(implicit p: Parameters) extends XSModule with HasBackendRedirectInfo with HasBPUParameter {
  val io = IO(new Bundle {
    val start_addr = Input(UInt(VAddrBits.W))
    val old_entry = Input(new FTBEntry)
    val pd = Input(new Ftq_pd_Entry)
    val cfiIndex = Flipped(Valid(UInt(log2Ceil(PredictWidth).W)))
    val target = Input(UInt(VAddrBits.W))
    val hit = Input(Bool())
    val mispredict_vec = Input(Vec(PredictWidth, Bool()))

    val new_entry = Output(new FTBEntry)
    val new_br_insert_pos = Output(Vec(numBr, Bool()))
    val taken_mask = Output(Vec(numBr, Bool()))
    val jmp_taken = Output(Bool())
    val mispred_mask = Output(Vec(numBr+1, Bool()))

    // for perf counters
    val is_init_entry = Output(Bool())
    val is_old_entry = Output(Bool())
    val is_new_br = Output(Bool())
    val is_jalr_target_modified = Output(Bool())
    val is_always_taken_modified = Output(Bool())
    val is_br_full = Output(Bool())
  })

  // no mispredictions detected at predecode
  val hit = io.hit
  val pd = io.pd

  val init_entry = WireInit(0.U.asTypeOf(new FTBEntry))


  val cfi_is_br = pd.brMask(io.cfiIndex.bits) && io.cfiIndex.valid
  val entry_has_jmp = pd.jmpInfo.valid
  val new_jmp_is_jal  = entry_has_jmp && !pd.jmpInfo.bits(0) && io.cfiIndex.valid
  val new_jmp_is_jalr = entry_has_jmp &&  pd.jmpInfo.bits(0) && io.cfiIndex.valid
  val new_jmp_is_call = entry_has_jmp &&  pd.jmpInfo.bits(1) && io.cfiIndex.valid
  val new_jmp_is_ret  = entry_has_jmp &&  pd.jmpInfo.bits(2) && io.cfiIndex.valid
  val last_jmp_rvi = entry_has_jmp && pd.jmpOffset === (PredictWidth-1).U && !pd.rvcMask.last
  // val last_br_rvi = cfi_is_br && io.cfiIndex.bits === (PredictWidth-1).U && !pd.rvcMask.last

  val cfi_is_jal = io.cfiIndex.bits === pd.jmpOffset && new_jmp_is_jal
  val cfi_is_jalr = io.cfiIndex.bits === pd.jmpOffset && new_jmp_is_jalr

  def carryPos = log2Ceil(PredictWidth)+instOffsetBits
  def getLower(pc: UInt) = pc(carryPos-1, instOffsetBits)
  // if not hit, establish a new entry
  init_entry.valid := true.B
  // tag is left for ftb to assign

  // case br
  val init_br_slot = init_entry.getSlotForBr(0)
  when (cfi_is_br) {
    init_br_slot.valid := true.B
    init_br_slot.offset := io.cfiIndex.bits
    init_br_slot.isRVC := pd.rvcMask(io.cfiIndex.bits)
    init_br_slot.setLowerStatByTarget(io.start_addr, io.target, numBr == 1)
    init_entry.always_taken(0) := true.B // set to always taken on init
  }

  // case jmp
  when (entry_has_jmp) {
    init_entry.tailSlot.offset := pd.jmpOffset
    init_entry.tailSlot.isRVC := pd.rvcMask(pd.jmpOffset)
    init_entry.tailSlot.valid := new_jmp_is_jal || new_jmp_is_jalr
    init_entry.tailSlot.setLowerStatByTarget(io.start_addr, Mux(cfi_is_jalr, io.target, pd.jalTarget), isShare=false)
  }

  val jmpPft = getLower(io.start_addr) +& pd.jmpOffset +& Mux(pd.rvcMask(pd.jmpOffset), 1.U, 2.U)
  init_entry.pftAddr := Mux(entry_has_jmp && !last_jmp_rvi, jmpPft, getLower(io.start_addr))
  init_entry.carry   := Mux(entry_has_jmp && !last_jmp_rvi, jmpPft(carryPos-instOffsetBits), true.B)
  init_entry.isJalr := new_jmp_is_jalr
  init_entry.isCall := new_jmp_is_call
  init_entry.isRet  := new_jmp_is_ret
  // that means fall thru points to the middle of an inst
  init_entry.last_may_be_rvi_call := pd.jmpOffset === (PredictWidth-1).U && !pd.rvcMask(pd.jmpOffset)

  // if hit, check whether a new cfi(only br is possible) is detected
  val oe = io.old_entry
  val br_recorded_vec = oe.getBrRecordedVec(io.cfiIndex.bits)
  val br_recorded = br_recorded_vec.asUInt.orR
  val is_new_br = cfi_is_br && !br_recorded
  val new_br_offset = io.cfiIndex.bits
  // vec(i) means new br will be inserted BEFORE old br(i)
  val allBrSlotsVec = oe.allSlotsForBr
  val new_br_insert_onehot = VecInit((0 until numBr).map{
    i => i match {
      case 0 =>
        !allBrSlotsVec(0).valid || new_br_offset < allBrSlotsVec(0).offset
      case idx =>
        allBrSlotsVec(idx-1).valid && new_br_offset > allBrSlotsVec(idx-1).offset &&
        (!allBrSlotsVec(idx).valid || new_br_offset < allBrSlotsVec(idx).offset)
    }
  })

  val old_entry_modified = WireInit(io.old_entry)
  for (i <- 0 until numBr) {
    val slot = old_entry_modified.allSlotsForBr(i)
    when (new_br_insert_onehot(i)) {
      slot.valid := true.B
      slot.offset := new_br_offset
      slot.isRVC := pd.rvcMask(new_br_offset)
      slot.setLowerStatByTarget(io.start_addr, io.target, i == numBr-1)
      old_entry_modified.always_taken(i) := true.B
    }.elsewhen (new_br_offset > oe.allSlotsForBr(i).offset) {
      old_entry_modified.always_taken(i) := false.B
      // all other fields remain unchanged
    }.otherwise {
      // case i == 0, remain unchanged
      if (i != 0) {
        val noNeedToMoveFromFormerSlot = (i == numBr-1).B && !oe.brSlots.last.valid
        when (!noNeedToMoveFromFormerSlot) {
          slot.fromAnotherSlot(oe.allSlotsForBr(i-1))
          old_entry_modified.always_taken(i) := oe.always_taken(i)
        }
      }
    }
  }

  // two circumstances:
  // 1. oe: | br | j  |, new br should be in front of j, thus addr of j should be new pft
  // 2. oe: | br | br |, new br could be anywhere between, thus new pft is the addr of either
  //        the previous last br or the new br
  val may_have_to_replace = oe.noEmptySlotForNewBr
  val pft_need_to_change = is_new_br && may_have_to_replace
  // it should either be the given last br or the new br
  when (pft_need_to_change) {
    val new_pft_offset =
      Mux(!new_br_insert_onehot.asUInt.orR,
        new_br_offset, oe.allSlotsForBr.last.offset)

    // set jmp to invalid
    old_entry_modified.pftAddr := getLower(io.start_addr) + new_pft_offset
    old_entry_modified.carry := (getLower(io.start_addr) +& new_pft_offset).head(1).asBool
    old_entry_modified.last_may_be_rvi_call := false.B
    old_entry_modified.isCall := false.B
    old_entry_modified.isRet := false.B
    old_entry_modified.isJalr := false.B
  }

  val old_entry_jmp_target_modified = WireInit(oe)
  val old_target = oe.tailSlot.getTarget(io.start_addr) // may be wrong because we store only 20 lowest bits
  val old_tail_is_jmp = !oe.tailSlot.sharing
  val jalr_target_modified = cfi_is_jalr && (old_target =/= io.target) && old_tail_is_jmp // TODO: pass full jalr target
  when (jalr_target_modified) {
    old_entry_jmp_target_modified.setByJmpTarget(io.start_addr, io.target)
    old_entry_jmp_target_modified.always_taken := 0.U.asTypeOf(Vec(numBr, Bool()))
  }

  val old_entry_always_taken = WireInit(oe)
  val always_taken_modified_vec = Wire(Vec(numBr, Bool())) // whether modified or not
  for (i <- 0 until numBr) {
    old_entry_always_taken.always_taken(i) :=
      oe.always_taken(i) && io.cfiIndex.valid && oe.brValids(i) && io.cfiIndex.bits === oe.brOffset(i)
    always_taken_modified_vec(i) := oe.always_taken(i) && !old_entry_always_taken.always_taken(i)
  }
  val always_taken_modified = always_taken_modified_vec.reduce(_||_)



  val derived_from_old_entry =
    Mux(is_new_br, old_entry_modified,
      Mux(jalr_target_modified, old_entry_jmp_target_modified, old_entry_always_taken))


  io.new_entry := Mux(!hit, init_entry, derived_from_old_entry)

  io.new_br_insert_pos := new_br_insert_onehot
  io.taken_mask := VecInit((io.new_entry.brOffset zip io.new_entry.brValids).map{
    case (off, v) => io.cfiIndex.bits === off && io.cfiIndex.valid && v
  })
  io.jmp_taken := io.new_entry.jmpValid && io.new_entry.tailSlot.offset === io.cfiIndex.bits
  for (i <- 0 until numBr) {
    io.mispred_mask(i) := io.new_entry.brValids(i) && io.mispredict_vec(io.new_entry.brOffset(i))
  }
  io.mispred_mask.last := io.new_entry.jmpValid && io.mispredict_vec(pd.jmpOffset)

  // for perf counters
  io.is_init_entry := !hit
  io.is_old_entry := hit && !is_new_br && !jalr_target_modified && !always_taken_modified
  io.is_new_br := hit && is_new_br
  io.is_jalr_target_modified := hit && jalr_target_modified
  io.is_always_taken_modified := hit && always_taken_modified
  io.is_br_full := hit && is_new_br && may_have_to_replace
}

class FtqPcMemWrapper(numOtherReads: Int)(implicit p: Parameters) extends XSModule with HasBackendRedirectInfo {
  val io = IO(new Bundle {
    val ifuPtr_w       = Input(new FtqPtr)
    val ifuPtrPlus1_w  = Input(new FtqPtr)
    val ifuPtrPlus2_w  = Input(new FtqPtr)
    val commPtr_w      = Input(new FtqPtr)
    val commPtrPlus1_w = Input(new FtqPtr)
    val ifuPtr_rdata       = Output(new Ftq_RF_Components)
    val ifuPtrPlus1_rdata  = Output(new Ftq_RF_Components)
    val ifuPtrPlus2_rdata  = Output(new Ftq_RF_Components)
    val commPtr_rdata      = Output(new Ftq_RF_Components)
    val commPtrPlus1_rdata = Output(new Ftq_RF_Components)

    val other_raddrs = Input(Vec(numOtherReads, UInt(log2Ceil(FtqSize).W)))
    val other_rdatas = Output(Vec(numOtherReads, new Ftq_RF_Components))

    val wen = Input(Bool())
    val waddr = Input(UInt(log2Ceil(FtqSize).W))
    val wdata = Input(new Ftq_RF_Components)
  })

  val num_pc_read = numOtherReads + 5
  val mem = Module(new SyncDataModuleTemplate(new Ftq_RF_Components, FtqSize,
    num_pc_read, 1, "FtqPC"))
  mem.io.wen(0)   := io.wen
  mem.io.waddr(0) := io.waddr
  mem.io.wdata(0) := io.wdata

  // read one cycle ahead for ftq local reads
  val raddr_vec = VecInit(io.other_raddrs ++
    Seq(io.ifuPtr_w.value, io.ifuPtrPlus1_w.value, io.ifuPtrPlus2_w.value, io.commPtrPlus1_w.value, io.commPtr_w.value))

  mem.io.raddr := raddr_vec

  io.other_rdatas       := mem.io.rdata.dropRight(5)
  io.ifuPtr_rdata       := mem.io.rdata.dropRight(4).last
  io.ifuPtrPlus1_rdata  := mem.io.rdata.dropRight(3).last
  io.ifuPtrPlus2_rdata  := mem.io.rdata.dropRight(2).last
  io.commPtrPlus1_rdata := mem.io.rdata.dropRight(1).last
  io.commPtr_rdata      := mem.io.rdata.last
}

class Ftq(implicit p: Parameters) extends XSModule with HasCircularQueuePtrHelper
  with HasBackendRedirectInfo with BPUUtils with HasBPUConst with HasPerfEvents
  with HasICacheParameters with LoopPredictorParams {
  val io = IO(new Bundle {
    val fromBpu = Flipped(new BpuToFtqIO)
    val fromIfu = Flipped(new IfuToFtqIO)
    val fromBackend = Flipped(new CtrlToFtqIO)

    val toBpu = new FtqToBpuIO
    val toIfu = new FtqToIfuIO
    val toICache = new FtqToICacheIO
    val toBackend = new FtqToCtrlIO

    val toPrefetch = new FtqPrefechBundle

    val toIBuffer = new FtqToIBuffer
    val loopToIBuffer = Decoupled(new LoopToArbiter)
    val loopArbiterRedirect = Valid(new FtqPtr)
    val toBypass = Valid(new BpuBypassUpdate)
    val loopFlags = Output(Vec(FtqSize, Bool()))
    val fromIBuffer = Flipped(new IBufferToFtq)

    val bpuInfo = new Bundle {
      val bpRight = Output(UInt(XLEN.W))
      val bpWrong = Output(UInt(XLEN.W))
    }

    val mmioCommitRead = Flipped(new mmioCommitRead)
    val fence = Input(new LoopCacheFenceBundle)

    // debug purpose only
    val BpuPtr = Output(new FtqPtr)
  })
  io.bpuInfo := DontCare

  val loop_cache_hit = Wire(Bool())
  val loop_cache_redirect_scheduled = RegInit(0.B)
  val loop_cache_redirect_wait_sent = RegInit(0.B)
  val loop_cache_redirect_scheduled_ptr = Reg(new FtqPtr)
  val loop_cache_ready = Wire(Bool())
  val loop_cache_pd_valid = Wire(Bool())
  val loop_cache_pd_data = Wire(new PredecodeWritebackBundle)
  val should_send_req = Wire(Bool())
  val backendRedirect = Wire(Valid(new Redirect))
  val backendRedirectReg = RegNext(backendRedirect)

  val bpu_last_stage_writeback = RegInit(VecInit(Seq.fill(FtqSize)(0.B)))

  val stage2Flush = backendRedirect.valid
  val backendFlush = stage2Flush || RegNext(stage2Flush)
  val ifuFlush = Wire(Bool())
  val loopFlush = Wire(Bool())

  val flush = stage2Flush || RegNext(stage2Flush)

  val allowBpuIn, allowToIfu = WireInit(false.B)
  val flushToIfu = !allowToIfu
  allowBpuIn := !ifuFlush && !loopFlush && !backendRedirect.valid && !backendRedirectReg.valid
  allowToIfu := !ifuFlush && !loopFlush && !backendRedirect.valid && !backendRedirectReg.valid

  def copyNum = 5
  val bpuPtr, ifuPtr, ifuWbPtr, commPtr = RegInit(FtqPtr(false.B, 0.U))
  val ifuPtrPlus1 = RegInit(FtqPtr(false.B, 1.U))
  val ifuPtrPlus2 = RegInit(FtqPtr(false.B, 2.U))
  val commPtrPlus1 = RegInit(FtqPtr(false.B, 1.U))
  val copied_ifu_ptr = Seq.fill(copyNum)(RegInit(FtqPtr(false.B, 0.U)))
  val copied_bpu_ptr = Seq.fill(copyNum)(RegInit(FtqPtr(false.B, 0.U)))
  require(FtqSize >= 4)
  val ifuPtr_write       = WireInit(ifuPtr)
  val ifuPtrPlus1_write  = WireInit(ifuPtrPlus1)
  val ifuPtrPlus2_write  = WireInit(ifuPtrPlus2)
  val ifuWbPtr_write     = WireInit(ifuWbPtr)
  val commPtr_write      = WireInit(commPtr)
  val commPtrPlus1_write = WireInit(commPtrPlus1)
  ifuPtr       := ifuPtr_write
  ifuPtrPlus1  := ifuPtrPlus1_write
  ifuPtrPlus2  := ifuPtrPlus2_write
  ifuWbPtr     := ifuWbPtr_write
  commPtr      := commPtr_write
  commPtrPlus1 := commPtrPlus1_write
  copied_ifu_ptr.map{ptr =>
    ptr := ifuPtr_write
    dontTouch(ptr)
  }
  val validEntries = distanceBetween(bpuPtr, commPtr)

  io.BpuPtr := bpuPtr

  // **********************************************************************
  // **************************** enq from bpu ****************************
  // **********************************************************************
  val new_entry_ready = validEntries < FtqSize.U
  io.fromBpu.resp.ready := new_entry_ready

  val bpu_s2_resp = io.fromBpu.resp.bits.s2
  val bpu_s3_resp = io.fromBpu.resp.bits.s3
  val bpu_s2_redirect = bpu_s2_resp.valid(dupForFtq) && bpu_s2_resp.hasRedirect(dupForFtq)
  val bpu_s3_redirect = bpu_s3_resp.valid(dupForFtq) && bpu_s3_resp.hasRedirect(dupForFtq)

  io.toBpu.enq_ptr := bpuPtr
  val enq_fire = io.fromBpu.resp.fire() && allowBpuIn // from bpu s1
  val bpu_in_fire = (io.fromBpu.resp.fire() || bpu_s2_redirect || bpu_s3_redirect) && allowBpuIn

  val bpu_in_resp = io.fromBpu.resp.bits.selectedRespForFtq
  val bpu_in_stage = io.fromBpu.resp.bits.selectedRespIdxForFtq
  val bpu_in_resp_ptr = Mux(bpu_in_stage === BP_S1, bpuPtr, bpu_in_resp.ftq_idx)
  val bpu_in_resp_idx = bpu_in_resp_ptr.value

  // read ports:      prefetchReq ++  ifuReq1 + ifuReq2 + ifuReq3 + commitUpdate2 + commitUpdate
  val ftq_pc_mem = Module(new FtqPcMemWrapper(1))
  // resp from uBTB
  ftq_pc_mem.io.wen := bpu_in_fire
  ftq_pc_mem.io.waddr := bpu_in_resp_idx
  ftq_pc_mem.io.wdata.fromBranchPrediction(bpu_in_resp)

  // record resp for loop inqueue
  val bpu_resp_mem = Reg(Vec(FtqSize, new BranchPredictionBundle))

  when (bpu_in_fire) {
    bpu_resp_mem(bpu_in_resp_idx) := bpu_in_resp
  }

  def ifuRedirectPort = 0
  def backendRedirectPort = 1
  def commitPort = 2
  def loopCacheBypassPort = 3
  //                                                            ifuRedirect + backendRedirect + commit + loopCacheBypass
  val ftq_redirect_sram = Module(new FtqNRSRAM(new Ftq_Redirect_SRAMEntry, 1+1+1+1))
  // these info is intended to enq at the last stage of bpu
  ftq_redirect_sram.io.wen := io.fromBpu.resp.bits.lastStage.valid(dupForFtq)
  ftq_redirect_sram.io.waddr := io.fromBpu.resp.bits.lastStage.ftq_idx.value
  ftq_redirect_sram.io.wdata := io.fromBpu.resp.bits.last_stage_spec_info
  println(f"ftq redirect SRAM: entry ${ftq_redirect_sram.io.wdata.getWidth} * ${FtqSize} * 3")
  println(f"ftq redirect SRAM: ahead fh ${ftq_redirect_sram.io.wdata.afhob.getWidth} * ${FtqSize} * 3")

  def ftq_meta_commitPort = 0
  def ftq_meta_loopCacheBypassPort = 1
  //                                                                commit + loopCacheBypass
  val ftq_meta_1r_sram = Module(new FtqNRSRAM(new Ftq_1R_SRAMEntry, 1+1))
  // these info is intended to enq at the last stage of bpu
  ftq_meta_1r_sram.io.wen := io.fromBpu.resp.bits.lastStage.valid(dupForFtq)
  ftq_meta_1r_sram.io.waddr := io.fromBpu.resp.bits.lastStage.ftq_idx.value
  ftq_meta_1r_sram.io.wdata.meta := io.fromBpu.resp.bits.last_stage_meta
  //                                                            ifuRedirect + backendRedirect + loopWriteback + commit + loopCacheBypass
  def ftb_mem_ifuRedirectPort = 0
  def ftb_mem_loopWritebackPort = 1
  def ftb_mem_backendRedirectPort = 2
  def ftb_mem_commitPort = 3
  def ftb_mem_loopCacheBypassPort = 4
  val ftb_entry_mem = Module(new SyncDataModuleTemplate(new FTBEntry, FtqSize, 1+1+1+1+1, 1, "FtqEntry"))
  ftb_entry_mem.io.wen(0) := io.fromBpu.resp.bits.lastStage.valid(dupForFtq)
  ftb_entry_mem.io.waddr(0) := io.fromBpu.resp.bits.lastStage.ftq_idx.value
  ftb_entry_mem.io.wdata(0) := io.fromBpu.resp.bits.last_stage_ftb_entry
  val lpPredInfo = WireDefault(0.U.asTypeOf(new xsLPpredInfo))

  when (io.fromBpu.resp.bits.lastStage.valid(dupForFtq) && !io.toBpu.redirect.valid) {
    bpu_last_stage_writeback(io.fromBpu.resp.bits.lastStage.ftq_idx.value) := true.B
  }

  // multi-write
  val update_target = Reg(Vec(FtqSize, UInt(VAddrBits.W))) // could be taken target or fallThrough //TODO: remove this
  val newest_entry_target = Reg(UInt(VAddrBits.W))
  val newest_entry_ptr = Reg(new FtqPtr)
  val cfiIndex_vec = Reg(Vec(FtqSize, ValidUndirectioned(UInt(log2Ceil(PredictWidth).W))))
  val mispredict_vec = Reg(Vec(FtqSize, Vec(PredictWidth, Bool())))
  val pred_stage = Reg(Vec(FtqSize, UInt(2.W)))

  val pdWb_flag = RegInit(VecInit(Seq.fill(FtqSize)(0.B)))
  val lpPredInfoArray = RegInit(VecInit(Seq.fill(64)(0.U.asTypeOf(new xsLPpredInfo))))
  val lpPred_flag = RegInit(VecInit(Seq.fill(FtqSize)(0.B)))
  val isDouble = RegInit(VecInit(Seq.fill(FtqSize)(0.B)))
  val isExit = RegInit(VecInit(Seq.fill(FtqSize)(0.B)))
  val isBypass = RegInit(VecInit(Seq.fill(FtqSize)(0.B)))
  val bypassCnt = Reg(Vec(FtqSize, UInt(cntBits.W)))

  // isDouble can only be set to true when Bypass provide prediction result at s3
  when (bpu_in_fire) {
    isDouble(bpu_in_resp_idx) := bpu_in_resp.isDouble
    isExit(bpu_in_resp_idx) := bpu_in_resp.isExit
    isBypass(bpu_in_resp_idx) := bpu_in_resp.fromBypass
    bypassCnt(bpu_in_resp_idx) := bpu_in_resp.remainCnt
  }

  // 1: loop 0: ifu
  val arbiter_flag = Reg(Vec(FtqSize, Bool()))
  io.loopFlags := arbiter_flag

  val loopArbiterRedirect = Wire(Valid(new FtqPtr))
  loopArbiterRedirect.valid := false.B
  loopArbiterRedirect.bits := DontCare
  io.loopArbiterRedirect := loopArbiterRedirect

  val c_invalid :: c_valid :: c_commited :: Nil = Enum(3)
  val commitStateQueue = RegInit(VecInit(Seq.fill(FtqSize) {
    VecInit(Seq.fill(PredictWidth)(c_invalid))
  }))

  val f_to_send :: f_sent :: Nil = Enum(2)
  val entry_fetch_status = RegInit(VecInit(Seq.fill(FtqSize)(f_sent)))

  val h_not_hit :: h_false_hit :: h_hit :: Nil = Enum(3)
  val entry_hit_status = RegInit(VecInit(Seq.fill(FtqSize)(h_not_hit)))

  // modify registers one cycle later to cut critical path
  val last_cycle_bpu_in = RegNext(bpu_in_fire)
  val last_cycle_bpu_in_ptr = RegNext(bpu_in_resp_ptr)
  val last_cycle_bpu_in_idx = last_cycle_bpu_in_ptr.value
  val last_cycle_bpu_target = RegNext(bpu_in_resp.target(dupForFtq))
  val last_cycle_cfiIndex = RegNext(bpu_in_resp.cfiIndex(dupForFtq))
  val last_cycle_bpu_in_stage = RegNext(bpu_in_stage)
  //val last_cycle_bpu_in_isExit = RegNext(bpu_in_resp.isExit)
  //val last_cycle_bpu_in_pc = RegNext(bpu_in_resp.pc(dupForFtq))
  //val last_cycle_bpu_in_rvc = RegNext(bpu_in_resp.full_pred(0).)

  def extra_copyNum_for_commitStateQueue = 2
  val copied_last_cycle_bpu_in = VecInit(Seq.fill(copyNum+extra_copyNum_for_commitStateQueue)(RegNext(bpu_in_fire)))
  val copied_last_cycle_bpu_in_ptr_for_ftq = VecInit(Seq.fill(extra_copyNum_for_commitStateQueue)(RegNext(bpu_in_resp_ptr)))

  when (last_cycle_bpu_in) {
    entry_fetch_status(last_cycle_bpu_in_idx) := f_to_send
    cfiIndex_vec(last_cycle_bpu_in_idx) := last_cycle_cfiIndex
    pred_stage(last_cycle_bpu_in_idx) := last_cycle_bpu_in_stage

    update_target(last_cycle_bpu_in_idx) := last_cycle_bpu_target // TODO: remove this
    newest_entry_target := last_cycle_bpu_target
    newest_entry_ptr := last_cycle_bpu_in_ptr
  }

  // reduce fanout by delay write for a cycle
  when (RegNext(last_cycle_bpu_in)) {
    mispredict_vec(RegNext(last_cycle_bpu_in_idx)) := WireInit(VecInit(Seq.fill(PredictWidth)(false.B)))
  }

  // reduce fanout using copied last_cycle_bpu_in and copied last_cycle_bpu_in_ptr
  val copied_last_cycle_bpu_in_for_ftq = copied_last_cycle_bpu_in.takeRight(extra_copyNum_for_commitStateQueue)
  copied_last_cycle_bpu_in_for_ftq.zip(copied_last_cycle_bpu_in_ptr_for_ftq).zipWithIndex.map {
    case ((in, ptr), i) =>
      when (in) {
        val perSetEntries = FtqSize / extra_copyNum_for_commitStateQueue // 32
        require(FtqSize % extra_copyNum_for_commitStateQueue == 0)
        for (j <- 0 until perSetEntries) {
          when (ptr.value === (i*perSetEntries+j).U) {
            commitStateQueue(i*perSetEntries+j) := VecInit(Seq.fill(PredictWidth)(c_invalid))
          }
        }
      }
  }

  // num cycle is fixed
  io.toBackend.newest_entry_ptr := RegNext(newest_entry_ptr)
  io.toBackend.newest_entry_target := RegNext(newest_entry_target)


  bpuPtr := bpuPtr + enq_fire
  copied_bpu_ptr.map(_ := bpuPtr + enq_fire)
  when ((io.toIfu.req.fire && allowToIfu) || (should_send_req && loop_cache_hit)) {
    ifuPtr_write := ifuPtrPlus1
    ifuPtrPlus1_write := ifuPtrPlus2
    ifuPtrPlus2_write := ifuPtrPlus2 + 1.U
  }

  // only use ftb result to assign hit status
  when (bpu_s2_resp.valid(dupForFtq)) {
    entry_hit_status(bpu_s2_resp.ftq_idx.value) := Mux(bpu_s2_resp.full_pred(dupForFtq).hit, h_hit, h_not_hit)
  }


  io.toIfu.flushFromBpu.s2.valid := bpu_s2_redirect
  io.toIfu.flushFromBpu.s2.bits := bpu_s2_resp.ftq_idx
  when (bpu_s2_redirect) {
    bpuPtr := bpu_s2_resp.ftq_idx + 1.U
    copied_bpu_ptr.map(_ := bpu_s2_resp.ftq_idx + 1.U)
    // only when ifuPtr runs ahead of bpu s2 resp should we recover it
    when (!isBefore(ifuPtr, bpu_s2_resp.ftq_idx)) {
      ifuPtr_write := bpu_s2_resp.ftq_idx
      ifuPtrPlus1_write := bpu_s2_resp.ftq_idx + 1.U
      ifuPtrPlus2_write := bpu_s2_resp.ftq_idx + 2.U
    }

    val idx = bpu_s2_resp.ftq_idx + 1.U
    val next_less = idx.value <= bpuPtr.value
    when (next_less) {
      bpu_last_stage_writeback.zipWithIndex.foreach({ case (f,i) =>
        when (i.U >= idx.value && i.U < bpuPtr.value) {
          // s2 redirect must clear from and include new ptr
          f := false.B
        }
      })

      lpPred_flag.zipWithIndex.foreach({ case (f,i) =>
        when (i.U >= idx.value && i.U < bpuPtr.value) {
          // s2 redirect must clear from and include new ptr
          f := false.B
        }
      })
    } .otherwise {
      bpu_last_stage_writeback.zipWithIndex.foreach({ case (f,i) =>
        when (i.U < bpuPtr.value || i.U >= idx.value) {
          // s2 redirect must clear from and include new ptr
          f := false.B
        }
      })

      lpPred_flag.zipWithIndex.foreach({ case (f,i) =>
        when (i.U < bpuPtr.value || i.U >= idx.value) {
          // s2 redirect must clear from and include new ptr
          f := false.B
        }
      })
    }
  }

  io.toIfu.flushFromBpu.s3.valid := bpu_s3_redirect
  io.toIfu.flushFromBpu.s3.bits := bpu_s3_resp.ftq_idx
  when (bpu_s3_redirect) {
    bpuPtr := bpu_s3_resp.ftq_idx + 1.U
    copied_bpu_ptr.map(_ := bpu_s3_resp.ftq_idx + 1.U)
    // only when ifuPtr runs ahead of bpu s2 resp should we recover it
    when (!isBefore(ifuPtr, bpu_s3_resp.ftq_idx)) {
      ifuPtr_write := bpu_s3_resp.ftq_idx
      ifuPtrPlus1_write := bpu_s3_resp.ftq_idx + 1.U
      ifuPtrPlus2_write := bpu_s3_resp.ftq_idx + 2.U
    }

    val idx = bpu_s3_resp.ftq_idx + 1.U
    val next_less = idx.value <= bpuPtr.value
    when (next_less) {
      bpu_last_stage_writeback.zipWithIndex.foreach({ case (f,i) =>
        when (i.U >= idx.value && i.U < bpuPtr.value) {
          // s3 redirect must clear from new ptr but the new ptr itself should be determined by s3 result
          f := false.B
        }
      })

      lpPred_flag.zipWithIndex.foreach({ case (f,i) =>
        when (i.U >= idx.value && i.U < bpuPtr.value) {
          f := false.B
        }
      })
    } .otherwise {
      bpu_last_stage_writeback.zipWithIndex.foreach({ case (f,i) =>
        when (i.U < bpuPtr.value || i.U >= idx.value) {
          f := false.B
        }
      })

      lpPred_flag.zipWithIndex.foreach({ case (f,i) =>
        when (i.U < bpuPtr.value || i.U >= idx.value) {
          f := false.B
        }
      })
    }
  }

  XSError(isBefore(bpuPtr, ifuPtr) && !isFull(bpuPtr, ifuPtr), "\nifuPtr is before bpuPtr!\n")
  XSError(isBefore(ifuPtr, ifuWbPtr) && !isFull(ifuPtr, ifuWbPtr), "\nifuWbPtr is before ifuPtr!\n")

  (0 until copyNum).map{i =>
    XSError(copied_bpu_ptr(i) =/= bpuPtr, "\ncopiedBpuPtr is different from bpuPtr!\n")
  }

  val loopMainCache = Module(new LoopCacheNonSpecEntry)
  // ****************************************************************
  // **************************** to ifu ****************************
  // ****************************************************************
  // 0  for ifu, and 1-4 for ICache
  val bpu_in_bypass_buf = RegEnable(ftq_pc_mem.io.wdata, enable=bpu_in_fire)
  val copied_bpu_in_bypass_buf = VecInit(Seq.fill(copyNum)(RegEnable(ftq_pc_mem.io.wdata, enable=bpu_in_fire)))
  val bpu_in_bypass_buf_for_ifu = bpu_in_bypass_buf
  val bpu_in_bypass_ptr = RegNext(bpu_in_resp_ptr)
  val last_cycle_to_ifu_fire = RegNext(io.toIfu.req.fire)
  val last_cycle_to_loop_fire = RegNext(loopMainCache.io.req.fire && loopMainCache.io.l0_hit)

  val copied_bpu_in_bypass_ptr = VecInit(Seq.fill(copyNum)(RegNext(bpu_in_resp_ptr)))
  val copied_last_cycle_to_ifu_fire = VecInit(Seq.fill(copyNum)(RegNext(io.toIfu.req.fire)))
  val copied_last_cycle_to_loop_fire = VecInit(Seq.fill(copyNum)(RegNext(loopMainCache.io.req.fire && loopMainCache.io.l0_hit)))

  // read pc and target
  ftq_pc_mem.io.ifuPtr_w       := ifuPtr_write
  ftq_pc_mem.io.ifuPtrPlus1_w  := ifuPtrPlus1_write
  ftq_pc_mem.io.ifuPtrPlus2_w  := ifuPtrPlus2_write
  ftq_pc_mem.io.commPtr_w      := commPtr_write
  ftq_pc_mem.io.commPtrPlus1_w := commPtrPlus1_write


  io.toIfu.req.bits.ftqIdx := ifuPtr

  val toICachePcBundle = Wire(Vec(copyNum,new Ftq_RF_Components))
  val toICacheEntryToSend = Wire(Vec(copyNum,Bool()))
  val toIfuPcBundle = Wire(new Ftq_RF_Components)
  val entry_is_to_send = WireInit(entry_fetch_status(ifuPtr.value) === f_to_send)
  val entry_ftq_offset = WireInit(cfiIndex_vec(ifuPtr.value))
  val entry_next_addr  = Wire(UInt(VAddrBits.W))

  val pc_mem_ifu_ptr_rdata   = VecInit(Seq.fill(copyNum)(RegNext(ftq_pc_mem.io.ifuPtr_rdata)))
  val pc_mem_ifu_plus1_rdata = VecInit(Seq.fill(copyNum)(RegNext(ftq_pc_mem.io.ifuPtrPlus1_rdata)))
  val diff_entry_next_addr = WireInit(update_target(ifuPtr.value)) //TODO: remove this

  val copied_ifu_plus1_to_send = VecInit(Seq.fill(copyNum)(RegNext(entry_fetch_status(ifuPtrPlus1.value) === f_to_send) || RegNext(last_cycle_bpu_in && bpu_in_bypass_ptr === (ifuPtrPlus1))))
  val copied_ifu_ptr_to_send   = VecInit(Seq.fill(copyNum)(RegNext(entry_fetch_status(ifuPtr.value) === f_to_send) || RegNext(last_cycle_bpu_in && bpu_in_bypass_ptr === ifuPtr)))

  for(i <- 0 until copyNum){
    when(copied_last_cycle_bpu_in(i) && copied_bpu_in_bypass_ptr(i) === copied_ifu_ptr(i)){
      toICachePcBundle(i) := copied_bpu_in_bypass_buf(i)
      toICacheEntryToSend(i)   := true.B
    }.elsewhen(copied_last_cycle_to_ifu_fire(i) || copied_last_cycle_to_loop_fire(i)){
      toICachePcBundle(i) := pc_mem_ifu_plus1_rdata(i)
      toICacheEntryToSend(i)   := copied_ifu_plus1_to_send(i)
    }.otherwise{
      toICachePcBundle(i) := pc_mem_ifu_ptr_rdata(i)
      toICacheEntryToSend(i)   := copied_ifu_ptr_to_send(i)
    }
  }

  // TODO: reconsider target address bypass logic
  when (last_cycle_bpu_in && bpu_in_bypass_ptr === ifuPtr) {
    toIfuPcBundle := bpu_in_bypass_buf_for_ifu
    entry_is_to_send := true.B
    entry_next_addr := last_cycle_bpu_target
    entry_ftq_offset := last_cycle_cfiIndex
    diff_entry_next_addr := last_cycle_bpu_target // TODO: remove this
  }.elsewhen (last_cycle_to_ifu_fire || last_cycle_to_loop_fire) {
    toIfuPcBundle := RegNext(ftq_pc_mem.io.ifuPtrPlus1_rdata)
    entry_is_to_send := RegNext(entry_fetch_status(ifuPtrPlus1.value) === f_to_send) ||
                        RegNext(last_cycle_bpu_in && bpu_in_bypass_ptr === (ifuPtrPlus1)) // reduce potential bubbles
    entry_next_addr := Mux(last_cycle_bpu_in && bpu_in_bypass_ptr === (ifuPtrPlus1),
                          bpu_in_bypass_buf_for_ifu.startAddr,
                          Mux(ifuPtr === newest_entry_ptr,
                            newest_entry_target,
                            RegNext(ftq_pc_mem.io.ifuPtrPlus2_rdata.startAddr))) // ifuPtr+2
  }.otherwise {
    toIfuPcBundle := RegNext(ftq_pc_mem.io.ifuPtr_rdata)
    entry_is_to_send := RegNext(entry_fetch_status(ifuPtr.value) === f_to_send) ||
                        RegNext(last_cycle_bpu_in && bpu_in_bypass_ptr === ifuPtr) // reduce potential bubbles
    entry_next_addr := Mux(last_cycle_bpu_in && bpu_in_bypass_ptr === (ifuPtrPlus1),
                          bpu_in_bypass_buf_for_ifu.startAddr,
                          Mux(ifuPtr === newest_entry_ptr,
                            newest_entry_target,
                            RegNext(ftq_pc_mem.io.ifuPtrPlus1_rdata.startAddr))) // ifuPtr+1
  }

  should_send_req := entry_is_to_send && ifuPtr =/= bpuPtr && loop_cache_ready && !loop_cache_redirect_wait_sent && lpPred_flag(ifuPtr.value)

  when (should_send_req) {
    arbiter_flag(ifuPtr.value) := loop_cache_hit
  }


  io.toIfu.req.valid := should_send_req && !loop_cache_hit //&& !RegNext(loopMainCache.io.req.valid && loop_cache_hit)
  io.toIfu.req.bits.nextStartAddr := entry_next_addr
  io.toIfu.req.bits.ftqOffset := entry_ftq_offset
  io.toIfu.req.bits.fromFtqPcBundle(toIfuPcBundle)

  io.toICache.req.valid := should_send_req && !loop_cache_hit //&& !RegNext(loopMainCache.io.req.valid && loop_cache_hit)
  io.toICache.req.bits.readValid.zipWithIndex.map{case(copy, i) => copy := toICacheEntryToSend(i) && copied_ifu_ptr(i) =/= copied_bpu_ptr(i)}
  io.toICache.req.bits.pcMemRead.zipWithIndex.map{case(copy,i) => copy.fromFtqPcBundle(toICachePcBundle(i))}
  // io.toICache.req.bits.bypassSelect := last_cycle_bpu_in && bpu_in_bypass_ptr === ifuPtr
  // io.toICache.req.bits.bpuBypassWrite.zipWithIndex.map{case(bypassWrtie, i) =>
  //   bypassWrtie.startAddr := bpu_in_bypass_buf.tail(i).startAddr
  //   bypassWrtie.nextlineStart := bpu_in_bypass_buf.tail(i).nextLineAddr
  // }

  // TODO: remove this
  XSError(io.toIfu.req.valid && diff_entry_next_addr =/= entry_next_addr,
          p"\nifu_req_target wrong! ifuPtr: ${ifuPtr}, entry_next_addr: ${Hexadecimal(entry_next_addr)} diff_entry_next_addr: ${Hexadecimal(diff_entry_next_addr)}\n")

  // when fall through is smaller in value than start address, there must be a false hit
  when (toIfuPcBundle.fallThruError && entry_hit_status(ifuPtr.value) === h_hit) {
    when (io.toIfu.req.fire &&
      !(bpu_s2_redirect && bpu_s2_resp.ftq_idx === ifuPtr) &&
      !(bpu_s3_redirect && bpu_s3_resp.ftq_idx === ifuPtr)
    ) {
      entry_hit_status(ifuPtr.value) := h_false_hit
      // XSError(true.B, "FTB false hit by fallThroughError, startAddr: %x, fallTHru: %x\n", io.toIfu.req.bits.startAddr, io.toIfu.req.bits.nextStartAddr)
    }
    XSDebug(true.B, "fallThruError! start:%x, fallThru:%x\n", io.toIfu.req.bits.startAddr, io.toIfu.req.bits.nextStartAddr)
  }

  XSPerfAccumulate(f"fall_through_error_to_ifu", toIfuPcBundle.fallThruError && entry_hit_status(ifuPtr.value) === h_hit &&
    io.toIfu.req.fire && !(bpu_s2_redirect && bpu_s2_resp.ftq_idx === ifuPtr) && !(bpu_s3_redirect && bpu_s3_resp.ftq_idx === ifuPtr))


  val ifu_req_should_be_flushed =
    ((io.toIfu.flushFromBpu.shouldFlushByStage2(io.toIfu.req.bits.ftqIdx) ||
    io.toIfu.flushFromBpu.shouldFlushByStage3(io.toIfu.req.bits.ftqIdx)) && io.toIfu.req.fire) ||
      ((loopMainCache.io.flushFromBpuIfu.shouldFlushByStage2(io.toIfu.req.bits.ftqIdx) ||
      loopMainCache.io.flushFromBpuIfu.shouldFlushByStage3(io.toIfu.req.bits.ftqIdx)) && should_send_req && loop_cache_hit)

    when ((io.toIfu.req.fire || (should_send_req && loop_cache_hit)) && !ifu_req_should_be_flushed) {
      entry_fetch_status(ifuPtr.value) := f_sent
    }

  // *********************************************************************
  // **************************** wb from ifu ****************************
  // *********************************************************************
  val loop_pd_wrapper = Wire(Valid(new PredecodeWritebackBundle))
  loop_pd_wrapper.valid := loop_cache_pd_valid
  loop_pd_wrapper.bits := loop_cache_pd_data
  val ifupdWb = io.fromIfu.pdWb

  // read ports:                                                         commit update
  val ftq_pd_mem = Module(new SyncDataModuleTemplate(new Ftq_pd_Entry, FtqSize, 1, 2, "FtqPd"))

  def handle_pdwb(pdWb: Valid[PredecodeWritebackBundle], writeport: Int) = {
    val pds = pdWb.bits.pd
    val ifu_wb_valid = pdWb.valid
    val ifu_wb_idx = pdWb.bits.ftqIdx.value
    ftq_pd_mem.io.wen(writeport) := ifu_wb_valid
    ftq_pd_mem.io.waddr(writeport) := pdWb.bits.ftqIdx.value
    ftq_pd_mem.io.wdata(writeport).fromPdWb(pdWb.bits)
    val hit_pd_valid = entry_hit_status(ifu_wb_idx) === h_hit && ifu_wb_valid
    val hit_pd_mispred = hit_pd_valid && pdWb.bits.misOffset.valid
    val hit_pd_mispred_reg = RegNext(hit_pd_mispred, init=false.B)
    val pd_reg       = RegEnable(pds,             enable = pdWb.valid)
    val start_pc_reg = RegEnable(pdWb.bits.pc(0), enable = pdWb.valid)
    val wb_idx_reg   = RegEnable(ifu_wb_idx,      enable = pdWb.valid)

    when (ifu_wb_valid) {
      // XSError(!isBefore(pdWb.bits.ftqIdx, ifuPtr), "predecode runahead of ifu")
      val comm_stq_wen = VecInit(pds.map(_.valid).zip(pdWb.bits.instrRange).map{
        case (v, inRange) => v && inRange
      })
      (commitStateQueue(ifu_wb_idx) zip comm_stq_wen).map{
        case (qe, v) => when (v) { qe := c_valid }
      }
      pdWb_flag(pdWb.bits.ftqIdx.value) := true.B
    }


    ftb_entry_mem.io.raddr(writeport) := ifu_wb_idx
    val has_false_hit = WireInit(false.B)
    when (RegNext(hit_pd_valid)) {
      // check for false hit
      val pred_ftb_entry = ftb_entry_mem.io.rdata(writeport)
      val brSlots = pred_ftb_entry.brSlots
      val tailSlot = pred_ftb_entry.tailSlot
      // we check cfis that bpu predicted

      // bpu predicted branches but denied by predecode
      val br_false_hit =
        brSlots.map{
          s => s.valid && !(pd_reg(s.offset).valid && pd_reg(s.offset).isBr)
        }.reduce(_||_) ||
          (tailSlot.valid && pred_ftb_entry.tailSlot.sharing &&
            !(pd_reg(tailSlot.offset).valid && pd_reg(tailSlot.offset).isBr))

      val jmpOffset = tailSlot.offset
      val jmp_pd = pd_reg(jmpOffset)
      val jal_false_hit = pred_ftb_entry.jmpValid &&
        ((pred_ftb_entry.isJal  && !(jmp_pd.valid && jmp_pd.isJal)) ||
          (pred_ftb_entry.isJalr && !(jmp_pd.valid && jmp_pd.isJalr)) ||
          (pred_ftb_entry.isCall && !(jmp_pd.valid && jmp_pd.isCall)) ||
          (pred_ftb_entry.isRet  && !(jmp_pd.valid && jmp_pd.isRet))
          )

      has_false_hit := br_false_hit || jal_false_hit || hit_pd_mispred_reg
      XSDebug(has_false_hit, "FTB false hit by br or jal or hit_pd, startAddr: %x\n", pdWb.bits.pc(0))

      // assert(!has_false_hit)
    }

    when (has_false_hit) {
      entry_hit_status(wb_idx_reg) := h_false_hit
    }

    ifu_wb_valid
  }

  handle_pdwb(ifupdWb, ftb_mem_ifuRedirectPort)
  handle_pdwb(loop_pd_wrapper, ftb_mem_loopWritebackPort)

  val pdWb = ifupdWb

  when (pdWb_flag(ifuWbPtr.value) === true.B && !isBefore(ifuPtr, ifuWbPtr + 1.U)) {
    ifuWbPtr_write := ifuWbPtr + 1.U
  }



  // **********************************************************************
  // ***************************** to backend *****************************
  // **********************************************************************
  // to backend pc mem / target
  io.toBackend.pc_mem_wen   := RegNext(last_cycle_bpu_in)
  io.toBackend.pc_mem_waddr := RegNext(last_cycle_bpu_in_idx)
  io.toBackend.pc_mem_wdata := RegNext(bpu_in_bypass_buf_for_ifu)

  // *******************************************************************************
  // **************************** redirect from backend ****************************
  // *******************************************************************************

  // redirect read cfiInfo, couples to redirectGen s2
  ftq_redirect_sram.io.ren(backendRedirectPort) := backendRedirect.valid
  ftq_redirect_sram.io.raddr(backendRedirectPort) := backendRedirect.bits.ftqIdx.value

  ftb_entry_mem.io.raddr(ftb_mem_backendRedirectPort) := backendRedirect.bits.ftqIdx.value

  val stage3CfiInfo = ftq_redirect_sram.io.rdata(backendRedirectPort)
  val fromBackendRedirect = WireInit(backendRedirectReg)
  val backendRedirectCfi = fromBackendRedirect.bits.cfiUpdate
  backendRedirectCfi.fromFtqRedirectSram(stage3CfiInfo)

  val r_ftb_entry = ftb_entry_mem.io.rdata(ftb_mem_backendRedirectPort)
  val r_ftqOffset = fromBackendRedirect.bits.ftqOffset

  when (entry_hit_status(fromBackendRedirect.bits.ftqIdx.value) === h_hit) {
    backendRedirectCfi.shift := PopCount(r_ftb_entry.getBrMaskByOffset(r_ftqOffset)) +&
      (backendRedirectCfi.pd.isBr && !r_ftb_entry.brIsSaved(r_ftqOffset) &&
      !r_ftb_entry.newBrCanNotInsert(r_ftqOffset))

    backendRedirectCfi.addIntoHist := backendRedirectCfi.pd.isBr && (r_ftb_entry.brIsSaved(r_ftqOffset) ||
        !r_ftb_entry.newBrCanNotInsert(r_ftqOffset))
  }.otherwise {
    backendRedirectCfi.shift := (backendRedirectCfi.pd.isBr && backendRedirectCfi.taken).asUInt
    backendRedirectCfi.addIntoHist := backendRedirectCfi.pd.isBr.asUInt
  }


  // ***************************************************************************
  // **************************** redirect from ifu ****************************
  // ***************************************************************************
  val fromIfuRedirect = WireInit(0.U.asTypeOf(Valid(new Redirect)))
  fromIfuRedirect.valid := pdWb.valid && pdWb.bits.misOffset.valid && !backendFlush
  fromIfuRedirect.bits.ftqIdx := pdWb.bits.ftqIdx
  fromIfuRedirect.bits.ftqOffset := pdWb.bits.misOffset.bits
  fromIfuRedirect.bits.level := RedirectLevel.flushAfter

  val ifuRedirectCfiUpdate = fromIfuRedirect.bits.cfiUpdate
  ifuRedirectCfiUpdate.pc := pdWb.bits.pc(pdWb.bits.misOffset.bits)
  ifuRedirectCfiUpdate.pd := pdWb.bits.pd(pdWb.bits.misOffset.bits)
  ifuRedirectCfiUpdate.predTaken := cfiIndex_vec(pdWb.bits.ftqIdx.value).valid
  ifuRedirectCfiUpdate.target := pdWb.bits.target
  ifuRedirectCfiUpdate.taken := pdWb.bits.cfiOffset.valid
  ifuRedirectCfiUpdate.isMisPred := pdWb.bits.misOffset.valid

  val ifuRedirectReg = RegNext(fromIfuRedirect, init=0.U.asTypeOf(Valid(new Redirect)))
  val ifuRedirectToBpu = WireInit(ifuRedirectReg)
  ifuFlush := fromIfuRedirect.valid || ifuRedirectToBpu.valid

  ftq_redirect_sram.io.ren(ifuRedirectPort) := fromIfuRedirect.valid
  ftq_redirect_sram.io.raddr(ifuRedirectPort) := fromIfuRedirect.bits.ftqIdx.value

  // ftb_entry_mem.io.raddr.head := fromIfuRedirect.bits.ftqIdx.value

  val toBpuCfi = ifuRedirectToBpu.bits.cfiUpdate
  toBpuCfi.fromFtqRedirectSram(ftq_redirect_sram.io.rdata(ifuRedirectPort))
  when (ifuRedirectReg.bits.cfiUpdate.pd.isRet) {
    toBpuCfi.target := toBpuCfi.rasEntry.retAddr
  }

  val fromLoopRedirect = Wire(Valid(new Redirect))
  val fromLoopRedirectReg = RegNext(fromLoopRedirect, init=0.U.asTypeOf(Valid(new Redirect)))
  loopFlush := fromLoopRedirect.valid || fromLoopRedirectReg.valid

  val loopRedirectToBpu = WireInit(fromLoopRedirectReg)

  // *********************************************************************
  // **************************** wb from exu ****************************
  // *********************************************************************

  backendRedirect := io.fromBackend.redirect

  def extractRedirectInfo(wb: Valid[Redirect]) = {
    val ftqPtr = wb.bits.ftqIdx
    val ftqOffset = wb.bits.ftqOffset
    val taken = wb.bits.cfiUpdate.taken
    val mispred = wb.bits.cfiUpdate.isMisPred
    (wb.valid, ftqPtr, ftqOffset, taken, mispred)
  }

  // fix mispredict entry
  val lastIsMispredict = RegNext(
    backendRedirect.valid && backendRedirect.bits.level === RedirectLevel.flushAfter, init = false.B
  )

  def updateCfiInfo(redirect: Valid[Redirect], isBackend: Boolean = true) = {
    val (r_valid, r_ptr, r_offset, r_taken, r_mispred) = extractRedirectInfo(redirect)
    val r_idx = r_ptr.value
    val cfiIndex_bits_wen = r_valid && r_taken && r_offset < cfiIndex_vec(r_idx).bits
    val cfiIndex_valid_wen = r_valid && r_offset === cfiIndex_vec(r_idx).bits
    when (cfiIndex_bits_wen || cfiIndex_valid_wen) {
      cfiIndex_vec(r_idx).valid := cfiIndex_bits_wen || cfiIndex_valid_wen && r_taken
    }
    when (cfiIndex_bits_wen) {
      cfiIndex_vec(r_idx).bits := r_offset
    }
    newest_entry_target := redirect.bits.cfiUpdate.target
    newest_entry_ptr := r_ptr
    update_target(r_idx) := redirect.bits.cfiUpdate.target // TODO: remove this
    if (isBackend) {
      mispredict_vec(r_idx)(r_offset) := r_mispred
    }
  }

  when(backendRedirectReg.valid) {
    updateCfiInfo(backendRedirectReg)
  }.elsewhen (ifuRedirectToBpu.valid) {
    updateCfiInfo(ifuRedirectToBpu, isBackend=false)
  } .elsewhen (loopRedirectToBpu.valid) {
    //updateCfiInfo(loopRedirectToBpu, isBackend = false)
    // does not update target for bpu-targeted loop redirect
  }

  // ***********************************************************************************
  // **************************** flush ptr and state queue ****************************
  // ***********************************************************************************

  val redirectVec = VecInit(backendRedirect, fromIfuRedirect, fromLoopRedirect)

  val loopCacheRedirectFlush = WireInit(false.B)

  loopCacheRedirectFlush := backendRedirect.valid // ifu flush should not flush loop cache as well
  // when redirect, we should reset ptrs and status queues
  when(redirectVec.map(r => r.valid).reduce(_||_)){
    val r = PriorityMux(redirectVec.map(r => (r.valid -> r.bits)))
    val notIfu = redirectVec.dropRight(2).map(r => r.valid).reduce(_||_) // not portable but breaks comb loop
    val notLoop = redirectVec.dropRight(1).map(r => r.valid).reduce(_||_)
    val (idx, offset, flushItSelf) = (r.ftqIdx, r.ftqOffset, RedirectLevel.flushItself(r.level))
    val next = idx + 1.U
    val isLoopRedirect = !redirectVec(0).valid && !redirectVec(1).valid
    bpuPtr := next
    copied_bpu_ptr.map(_ := next)
    ifuPtr_write := next
    when (!isLoopRedirect || isBefore(next, ifuWbPtr)) {
      ifuWbPtr_write := next
    }
    ifuPtrPlus1_write := idx + 2.U
    ifuPtrPlus2_write := idx + 3.U

    when (notIfu) {
      commitStateQueue(idx.value).zipWithIndex.foreach({ case (s, i) =>
        when(i.U > offset || i.U === offset && flushItSelf){
          s := c_invalid
        }
      })
    }
    when (backendRedirect.valid) {
      loopArbiterRedirect.valid := true.B
      loopArbiterRedirect.bits := backendRedirect.bits.ftqIdx + 1.U
    } .elsewhen(fromIfuRedirect.valid) {
      loopArbiterRedirect.valid := true.B
      loopArbiterRedirect.bits := fromIfuRedirect.bits.ftqIdx + 1.U
    }

    val next_less_bpu = idx.value < bpuPtr.value
    when (next_less_bpu) {
      bpu_last_stage_writeback.zipWithIndex.foreach({ case (f,i) =>
        when (i.U > idx.value && i.U < bpuPtr.value) {
          f := false.B
        }
      })

      lpPred_flag.zipWithIndex.foreach({ case (f,i) =>
        when(i.U > idx.value && i.U < bpuPtr.value) {
          f := false.B
        }
      })
    } .otherwise {
      bpu_last_stage_writeback.zipWithIndex.foreach({ case (f,i) =>
        when (i.U < bpuPtr.value || i.U > idx.value) {
          f := false.B
        }
      })

      lpPred_flag.zipWithIndex.foreach({ case (f,i) =>
        when (i.U < bpuPtr.value || i.U > idx.value) {
          f := false.B
        }
      })
    }

    val next_less = idx.value < ifuPtr.value
    when (next_less) {
      pdWb_flag.zipWithIndex.foreach({ case (f, i) =>
        when((i.U > idx.value && i.U <= ifuPtr.value)) {
          f := false.B
        }
      })
    } .otherwise {
      pdWb_flag.zipWithIndex.foreach({ case (f, i) =>
        when(i.U <= ifuPtr.value || i.U > idx.value) {
          f := false.B
        }
      })
    }
  }

  // only the valid bit is actually needed
  io.toIfu.redirect.bits    := backendRedirect.bits
  io.toIfu.redirect.valid   := stage2Flush

  // commit
  for (c <- io.fromBackend.rob_commits) {
    when(c.valid) {
      commitStateQueue(c.bits.ftqIdx.value)(c.bits.ftqOffset) := c_commited
      // TODO: remove this
      // For instruction fusions, we also update the next instruction
      when (c.bits.commitType === 4.U) {
        commitStateQueue(c.bits.ftqIdx.value)(c.bits.ftqOffset + 1.U) := c_commited
      }.elsewhen(c.bits.commitType === 5.U) {
        commitStateQueue(c.bits.ftqIdx.value)(c.bits.ftqOffset + 2.U) := c_commited
      }.elsewhen(c.bits.commitType === 6.U) {
        val index = (c.bits.ftqIdx + 1.U).value
        commitStateQueue(index)(0) := c_commited
      }.elsewhen(c.bits.commitType === 7.U) {
        val index = (c.bits.ftqIdx + 1.U).value
        commitStateQueue(index)(1) := c_commited
      }
    }
  }

  // ****************************************************************
  // **************************** to bpu ****************************
  // ****************************************************************

  io.toBpu.redirect <> Mux(fromBackendRedirect.valid, fromBackendRedirect, Mux(ifuRedirectToBpu.valid, ifuRedirectToBpu, fromLoopRedirectReg))

  XSError(backendRedirect.valid && isBefore(backendRedirect.bits.ftqIdx, commPtr), "Backend redirect should not come from committed slot")

  val may_have_stall_from_bpu = Wire(Bool())
  val bpu_ftb_update_stall = RegInit(0.U(2.W)) // 2-cycle stall, so we need 3 states
  may_have_stall_from_bpu := bpu_ftb_update_stall =/= 0.U
  val canCommit = commPtr =/= ifuWbPtr && !may_have_stall_from_bpu &&
    Cat(commitStateQueue(commPtr.value).map(s => {
      s === c_invalid || s === c_commited
    })).andR()

  val mmioReadPtr = io.mmioCommitRead.mmioFtqPtr
  val mmioLastCommit = isBefore(commPtr, mmioReadPtr) && (isAfter(ifuPtr,mmioReadPtr)  ||  mmioReadPtr ===   ifuPtr) &&
                       Cat(commitStateQueue(mmioReadPtr.value).map(s => { s === c_invalid || s === c_commited})).andR()
  io.mmioCommitRead.mmioLastCommit := RegNext(mmioLastCommit)

  // commit reads
  val commit_pc_bundle = RegNext(ftq_pc_mem.io.commPtr_rdata)
  val commit_target =
    Mux(RegNext(commPtr === newest_entry_ptr),
      RegNext(newest_entry_target),
      RegNext(ftq_pc_mem.io.commPtrPlus1_rdata.startAddr))
  ftq_pd_mem.io.raddr.last := commPtr.value
  val commit_pd = ftq_pd_mem.io.rdata.last
  ftq_redirect_sram.io.ren(commitPort) := canCommit
  ftq_redirect_sram.io.raddr(commitPort) := commPtr.value
  val commit_spec_meta = ftq_redirect_sram.io.rdata(commitPort)
  ftq_meta_1r_sram.io.ren(ftq_meta_commitPort) := canCommit
  ftq_meta_1r_sram.io.raddr(ftq_meta_commitPort) := commPtr.value
  val commit_meta = ftq_meta_1r_sram.io.rdata(ftq_meta_commitPort)
  ftb_entry_mem.io.raddr(ftb_mem_commitPort) := commPtr.value
  val commit_ftb_entry = ftb_entry_mem.io.rdata(ftb_mem_commitPort)

  // need one cycle to read mem and srams
  val do_commit_ptr = RegNext(commPtr)
  val do_commit = RegNext(canCommit, init=false.B)
  when (canCommit) {
    commPtr_write := commPtrPlus1
    commPtrPlus1_write := commPtrPlus1 + 1.U
    pdWb_flag(commPtr.value) := false.B
    lpPred_flag(commPtr.value) := false.B
    bpu_last_stage_writeback(commPtr.value) := false.B
  }
  val commit_state = RegNext(commitStateQueue(commPtr.value))
  val can_commit_cfi = WireInit(cfiIndex_vec(commPtr.value))
  when (commitStateQueue(commPtr.value)(can_commit_cfi.bits) =/= c_commited) {
    can_commit_cfi.valid := false.B
  }
  val commit_cfi = RegNext(can_commit_cfi)

  val commit_mispredict = VecInit((RegNext(mispredict_vec(commPtr.value)) zip commit_state).map {
    case (mis, state) => mis && state === c_commited
  })
  val can_commit_hit = entry_hit_status(commPtr.value)
  val commit_hit = RegNext(can_commit_hit)
  val diff_commit_target = RegNext(update_target(commPtr.value)) // TODO: remove this
  val commit_stage = RegNext(pred_stage(commPtr.value))
  val commit_valid = commit_hit === h_hit || commit_cfi.valid // hit or taken

  val to_bpu_hit = can_commit_hit === h_hit || can_commit_hit === h_false_hit
  switch (bpu_ftb_update_stall) {
    is (0.U) {
      when (can_commit_cfi.valid && !to_bpu_hit && canCommit) {
        bpu_ftb_update_stall := 2.U // 2-cycle stall
      }
    }
    is (2.U) {
      bpu_ftb_update_stall := 1.U
    }
    is (1.U) {
      bpu_ftb_update_stall := 0.U
    }
    is (3.U) {
      XSError(true.B, "bpu_ftb_update_stall should be 0, 1 or 2")
    }
  }

  // TODO: remove this
  // FIXME: diff commit target should reflect correct next start address
  XSError(do_commit && diff_commit_target =/= commit_target, "\ncommit target should be the same as update target\n")

  val prev_commit_target = RegInit(0.U.asTypeOf(UInt(VAddrBits.W)))
  val prev_commit_cfi_idx = RegInit(0.U.asTypeOf(Valid(UInt(log2Ceil(PredictWidth).W))))
  val prev_commit_pc = RegInit(0.U.asTypeOf(UInt(VAddrBits.W)))
  val commit_is_loop = commit_target === prev_commit_target && commit_cfi.bits === prev_commit_cfi_idx.bits && commit_cfi.valid && prev_commit_cfi_idx.valid && commit_pc_bundle.startAddr === prev_commit_pc && prev_commit_pc === prev_commit_target

  val valid_loop = RegInit(0.B)
  val valid_loop_pc = Reg(UInt(VAddrBits.W))
  val valid_loop_cfiIndex = Reg(UInt(log2Ceil(PredictWidth).W))

  io.toIBuffer.pc.valid := false.B
  io.toIBuffer.pc.bits := DontCare
  when (commit_is_loop) {
    valid_loop := true.B
    valid_loop_pc := prev_commit_target
    valid_loop_cfiIndex := prev_commit_cfi_idx.bits

    io.toIBuffer.pc.valid := valid_loop
    io.toIBuffer.pc.bits := valid_loop_pc
    // TODO: valid_loop pull down when inject
  }


  loopMainCache.io.update := io.fromIBuffer.update
  loopMainCache.io.flush := io.fence
  loopMainCache.io.flushFromBpuIfu.s2.valid := bpu_s2_redirect
  loopMainCache.io.flushFromBpuIfu.s2.bits := bpu_s2_resp.ftq_idx
  loopMainCache.io.flushFromBpuIfu.s3.valid := bpu_s3_redirect
  loopMainCache.io.flushFromBpuIfu.s3.bits := bpu_s3_resp.ftq_idx
  loopMainCache.io.flushFromBpuIfu.ifu.valid := ifuRedirectReg.valid
  loopMainCache.io.flushFromBpuIfu.ifu.bits := ifuRedirectReg.bits.ftqIdx

  io.toBpu.update := DontCare
  io.toBpu.update.valid := commit_valid && do_commit && !arbiter_flag(do_commit_ptr.value)
  val update = io.toBpu.update.bits
  update.false_hit   := commit_hit === h_false_hit
  update.pc          := commit_pc_bundle.startAddr
  update.meta        := commit_meta.meta
  update.cfi_idx     := commit_cfi
  update.full_target := commit_target
  update.from_stage  := commit_stage
  update.spec_info   := commit_spec_meta

  loopMainCache.io.req.valid := entry_is_to_send && ifuPtr =/= bpuPtr /*&& !RegNext(loopMainCache.io.req.valid && loop_cache_hit)*/ && !loop_cache_redirect_wait_sent && lpPred_flag(ifuPtr.value)
  loopMainCache.io.req.bits.pc := io.toIfu.req.bits.startAddr
  loopMainCache.io.req.bits.cfiIndex := Mux(io.toIfu.req.bits.ftqOffset.valid, io.toIfu.req.bits.ftqOffset.bits, 0xfffffff.U)
  loopMainCache.io.req.bits.cfiValid := io.toIfu.req.bits.ftqOffset.valid
  loopMainCache.io.req.bits.target := io.toIfu.req.bits.nextStartAddr

  loopMainCache.io.req.bits.ftqPtr := io.toIfu.req.bits.ftqIdx

  loopMainCache.io.req.bits.lpInfo.isInterNumGT2 := lpPredInfoArray(ifuPtr.value).isInterNumGT2
  loopMainCache.io.req.bits.lpInfo.isConfExitLoop := lpPredInfoArray(ifuPtr.value).isConfExitLoop
  loopMainCache.io.req.bits.lpInfo.remainIterNum := lpPredInfoArray(ifuPtr.value).remainIterNum
  loopMainCache.io.req.bits.lpInfo.isConf := lpPredInfoArray(ifuPtr.value).isConf
  // unused
  loopMainCache.io.req.bits.lpInfo.target := io.toIfu.req.bits.nextStartAddr

  loopMainCache.io.req.bits.isDouble := isDouble(ifuPtr.value)
  loopMainCache.io.req.bits.isExit := isExit(ifuPtr.value)
  loopMainCache.io.req.bits.bpu_in := bpu_resp_mem(ifuPtr.value)
  loopMainCache.io.resp.ready := true.B
  io.toBypass := loopMainCache.io.toBypass
  fromLoopRedirect := loopMainCache.io.toFtqRedirect

  loopMainCache.io.last_stage_info.valid := RegNext(loop_cache_redirect_scheduled && bpu_last_stage_writeback(loop_cache_redirect_scheduled_ptr.value))
  loopMainCache.io.last_stage_info.bits.ftqIdx := RegNext(loop_cache_redirect_scheduled_ptr)
  loopMainCache.io.last_stage_info.bits.last_stage_meta := ftq_meta_1r_sram.io.rdata(ftq_meta_loopCacheBypassPort).meta
  loopMainCache.io.last_stage_info.bits.last_stage_spec_info := ftq_redirect_sram.io.rdata(loopCacheBypassPort)
  loopMainCache.io.last_stage_info.bits.last_stage_ftb_entry := RegNext(ftb_entry_mem.io.rdata(ftb_mem_loopCacheBypassPort))

  loopMainCache.io.redirectFlush := loopCacheRedirectFlush

  loop_cache_hit := loopMainCache.io.l0_hit

  val loop_cache_scheduled_flush_by_Bpu_Ifu = loopMainCache.io.flushFromBpuIfu.shouldFlushByIfu(loop_cache_redirect_scheduled_ptr) ||
    loopMainCache.io.flushFromBpuIfu.shouldFlushByStage2(loop_cache_redirect_scheduled_ptr) ||
    loopMainCache.io.flushFromBpuIfu.shouldFlushByStage3(loop_cache_redirect_scheduled_ptr)


  /*
  * Loop cache redirect need two stage block
  * The first stage (loop_cache_redirect_scheduled) controlls the time to read ftq redirect sram, at the aim of providing
  * meta info for incoming redirect which is cleared by BPU last stage writeback
  * The second stage (loop_cache_redirect_wait_sent) controlls whether new queries can be sent to loop cache
  * which is cleared by the redirect from Loop Cache
  * */
  when (loop_cache_redirect_scheduled && (loopCacheRedirectFlush || loop_cache_scheduled_flush_by_Bpu_Ifu)) {
    loop_cache_redirect_scheduled := false.B
  }.elsewhen (loop_cache_redirect_scheduled && bpu_last_stage_writeback(loop_cache_redirect_scheduled_ptr.value)) {
    loop_cache_redirect_scheduled := false.B
  }.elsewhen (loopMainCache.io.req.valid && loopMainCache.io.l0_redirect_scheduled) {
    loop_cache_redirect_scheduled := true.B
    loop_cache_redirect_scheduled_ptr := loopMainCache.io.req.bits.ftqPtr
  }

  when (loop_cache_redirect_wait_sent && (loopCacheRedirectFlush || loop_cache_scheduled_flush_by_Bpu_Ifu)) {
    loop_cache_redirect_wait_sent := false.B
  } .elsewhen (loopMainCache.io.toFtqRedirect.valid && loop_cache_redirect_wait_sent) {
    loop_cache_redirect_wait_sent := false.B
  } .elsewhen (loopMainCache.io.req.valid && loopMainCache.io.l0_redirect_scheduled) {
    loop_cache_redirect_wait_sent := true.B
  }

  // loopCache last stage meta should return when corresponding info writeback
  ftq_redirect_sram.io.ren(loopCacheBypassPort) := loop_cache_redirect_scheduled && bpu_last_stage_writeback(loop_cache_redirect_scheduled_ptr.value)
  ftq_redirect_sram.io.raddr(loopCacheBypassPort) := loop_cache_redirect_scheduled_ptr.value

  ftq_meta_1r_sram.io.ren(ftq_meta_loopCacheBypassPort) := loop_cache_redirect_scheduled && bpu_last_stage_writeback(loop_cache_redirect_scheduled_ptr.value)
  ftq_meta_1r_sram.io.raddr(ftq_meta_loopCacheBypassPort) := loop_cache_redirect_scheduled_ptr.value

  ftb_entry_mem.io.raddr(ftb_mem_loopCacheBypassPort) := loop_cache_redirect_scheduled_ptr.value

  loop_cache_ready := loopMainCache.io.req.ready

  io.loopToIBuffer <> loopMainCache.io.resp

  loop_cache_pd_valid := loopMainCache.io.pd_valid
  loop_cache_pd_data := loopMainCache.io.pd_data
  // update.is_loop     := commit_is_loop

  when (commit_valid && do_commit) {
    prev_commit_target := commit_target
    prev_commit_cfi_idx := commit_cfi
    prev_commit_pc := commit_pc_bundle.startAddr
  }

  val commit_real_hit = commit_hit === h_hit
  val update_ftb_entry = update.ftb_entry

  val ftbEntryGen = Module(new FTBEntryGen).io
  ftbEntryGen.start_addr     := commit_pc_bundle.startAddr
  ftbEntryGen.old_entry      := commit_ftb_entry
  ftbEntryGen.pd             := commit_pd
  ftbEntryGen.cfiIndex       := commit_cfi
  ftbEntryGen.target         := commit_target
  ftbEntryGen.hit            := commit_real_hit
  ftbEntryGen.mispredict_vec := commit_mispredict

  update_ftb_entry         := ftbEntryGen.new_entry
  update.new_br_insert_pos := ftbEntryGen.new_br_insert_pos
  update.mispred_mask      := ftbEntryGen.mispred_mask
  update.old_entry         := ftbEntryGen.is_old_entry
  update.pred_hit          := commit_hit === h_hit || commit_hit === h_false_hit
  update.br_taken_mask     := ftbEntryGen.taken_mask
  update.jmp_taken         := ftbEntryGen.jmp_taken

  // update.full_pred.fromFtbEntry(ftbEntryGen.new_entry, update.pc)
  // update.full_pred.jalr_target := commit_target
  // update.full_pred.hit := true.B
  // when (update.full_pred.is_jalr) {
  //   update.full_pred.targets.last := commit_target
  // }

  // ****************************************************************
  // *********************** to prefetch ****************************
  // ****************************************************************

  ftq_pc_mem.io.other_raddrs(0) := DontCare
  if(cacheParams.hasPrefetch){
    val prefetchPtr = RegInit(FtqPtr(false.B, 0.U))
    val diff_prefetch_addr = WireInit(update_target(prefetchPtr.value)) //TODO: remove this

    prefetchPtr := prefetchPtr + io.toPrefetch.req.fire()

    ftq_pc_mem.io.other_raddrs(0) := prefetchPtr.value

    when (bpu_s2_redirect && !isBefore(prefetchPtr, bpu_s2_resp.ftq_idx)) {
      prefetchPtr := bpu_s2_resp.ftq_idx
    }

    when (bpu_s3_redirect && !isBefore(prefetchPtr, bpu_s3_resp.ftq_idx)) {
      prefetchPtr := bpu_s3_resp.ftq_idx
      // XSError(true.B, "\ns3_redirect mechanism not implemented!\n")
    }


    val prefetch_is_to_send = WireInit(entry_fetch_status(prefetchPtr.value) === f_to_send)
    val prefetch_addr = Wire(UInt(VAddrBits.W))

    when (last_cycle_bpu_in && bpu_in_bypass_ptr === prefetchPtr) {
      prefetch_is_to_send := true.B
      prefetch_addr := last_cycle_bpu_target
      diff_prefetch_addr := last_cycle_bpu_target // TODO: remove this
    }.otherwise{
      prefetch_addr := RegNext( ftq_pc_mem.io.other_rdatas(0).startAddr)
    }
    io.toPrefetch.req.valid := prefetchPtr =/= bpuPtr && prefetch_is_to_send
    io.toPrefetch.req.bits.target := prefetch_addr

    when(redirectVec.map(r => r.valid).reduce(_||_)){
      val r = PriorityMux(redirectVec.map(r => (r.valid -> r.bits)))
      val next = r.ftqIdx + 1.U
      prefetchPtr := next
    }

    // TODO: remove this
    // XSError(io.toPrefetch.req.valid && diff_prefetch_addr =/= prefetch_addr,
    //         f"\nprefetch_req_target wrong! prefetchPtr: ${prefetchPtr}, prefetch_addr: ${Hexadecimal(prefetch_addr)} diff_prefetch_addr: ${Hexadecimal(diff_prefetch_addr)}\n")


    XSError(isBefore(bpuPtr, prefetchPtr) && !isFull(bpuPtr, prefetchPtr), "\nprefetchPtr is before bpuPtr!\n")
    XSError(isBefore(prefetchPtr, ifuPtr) && !isFull(ifuPtr, prefetchPtr), "\nifuPtr is before prefetchPtr!\n")
  }
  else {
    io.toPrefetch.req <> DontCare
  }


  val xsLP = Module(new XSLoopPredictor)
  xsLP.io.lpEna := true.B
  
  val lpWriteSramEna = last_cycle_bpu_in 
  val lpWriteSramIdx = last_cycle_bpu_in_ptr.value
  
  xsLP.io.pred.valid := lpWriteSramEna 
  xsLP.io.pred.pc    := RegNext(bpu_in_resp.pc(dupForFtq)) 
  
  val lpMetaSram = Module(new FtqNRSRAM(new LPmeta, 1+1))// redirect+update
  lpMetaSram.io.wen   := lpWriteSramEna
  lpMetaSram.io.waddr := lpWriteSramIdx
  lpMetaSram.io.wdata := xsLP.io.pred.meta

  lpPredInfo.isConf         := xsLP.io.pred.isConf
  lpPredInfo.remainIterNum  := xsLP.io.pred.remainIterNum
  lpPredInfo.isConfExitLoop := xsLP.io.pred.isConfExitLoop
  lpPredInfo.target         := xsLP.io.pred.target
  lpPredInfo.isInterNumGT2  := xsLP.io.pred.isInterNumGT2
  
  when(xsLP.io.pred.valid) {
    lpPredInfoArray(lpWriteSramIdx) := lpPredInfo
    lpPred_flag(lpWriteSramIdx)     := true.B
  }

  // val lpRdrctSram = Module(new FtqNRSRAM(new lpRedirectInfo, 1))
  // lpRdrctSram.io.wen   := lpWriteSramEna
  // lpRdrctSram.io.waddr := lpWriteSramIdx
  // lpRdrctSram.io.wdata := xsLP.io.pred.lpInfo

  lpMetaSram.io.ren(0)   := backendRedirect.valid
  lpMetaSram.io.raddr(0) := backendRedirect.bits.ftqIdx.value
  val lpRedirectMeta = lpMetaSram.io.rdata(0)

  xsLP.io.redirect.valid := (backendRedirectReg.valid && lpRedirectMeta.lpPredInfo.predExitLoop
                             && backendRedirectReg.bits.cfiUpdate.taken) ||
                            (backendRedirectReg.valid && !lpRedirectMeta.lpPredInfo.predExitLoop 
                              && !backendRedirectReg.bits.cfiUpdate.taken)
  xsLP.io.redirect.meta := lpRedirectMeta

    // (lpInfo.isLPpred && backendRedirectReg.bits.cfiUpdate.taken && 
    // lpInfo.isPredNotTaken && backendRedirectReg.valid) ||
    // (lpInfo.isLPpred && !backendRedirectReg.bits.cfiUpdate.taken && 
    // lpInfo.isPredTaken && backendRedirectReg.valid)
  

  lpMetaSram.io.ren(1)   := true.B //canCommit // 
  lpMetaSram.io.raddr(1) := commPtr.value 
  val lpUpdateMeta        = lpMetaSram.io.rdata(1)

  xsLP.io.update.valid        := commit_valid && do_commit
  xsLP.io.update.pc           := commit_pc_bundle.startAddr 
  xsLP.io.update.isLoopBranch := commit_is_loop
  xsLP.io.update.updateTaken  := ftbEntryGen.taken_mask(0)
  xsLP.io.update.meta         := lpUpdateMeta
  // xsLP.io.update.target       := commit_target

  
  // ******************************************************************************
  // **************************** commit perf counters ****************************
  // ******************************************************************************

  val commit_inst_mask    = VecInit(commit_state.map(c => c === c_commited && do_commit)).asUInt
  val commit_mispred_mask = commit_mispredict.asUInt
  val commit_not_mispred_mask = ~commit_mispred_mask

  val commit_br_mask = commit_pd.brMask.asUInt
  val commit_jmp_mask = UIntToOH(commit_pd.jmpOffset) & Fill(PredictWidth, commit_pd.jmpInfo.valid.asTypeOf(UInt(1.W)))
  val commit_cfi_mask = (commit_br_mask | commit_jmp_mask)

  val mbpInstrs = commit_inst_mask & commit_cfi_mask

  val mbpRights = mbpInstrs & commit_not_mispred_mask
  val mbpWrongs = mbpInstrs & commit_mispred_mask

  io.bpuInfo.bpRight := PopCount(mbpRights)
  io.bpuInfo.bpWrong := PopCount(mbpWrongs)

  // Cfi Info
  for (i <- 0 until PredictWidth) {
    val pc = commit_pc_bundle.startAddr + (i * instBytes).U
    val v = commit_state(i) === c_commited
    val isBr = commit_pd.brMask(i)
    val isJmp = commit_pd.jmpInfo.valid && commit_pd.jmpOffset === i.U
    val isCfi = isBr || isJmp
    val isTaken = commit_cfi.valid && commit_cfi.bits === i.U
    val misPred = commit_mispredict(i)
    // val ghist = commit_spec_meta.ghist.predHist
    val histPtr = commit_spec_meta.histPtr
    val predCycle = commit_meta.meta(63, 0)
    val target = commit_target

    val brIdx = OHToUInt(Reverse(Cat(update_ftb_entry.brValids.zip(update_ftb_entry.brOffset).map{case(v, offset) => v && offset === i.U})))
    val inFtbEntry = update_ftb_entry.brValids.zip(update_ftb_entry.brOffset).map{case(v, offset) => v && offset === i.U}.reduce(_||_)
    val addIntoHist = ((commit_hit === h_hit) && inFtbEntry) || ((!(commit_hit === h_hit) && i.U === commit_cfi.bits && isBr && commit_cfi.valid))
    XSDebug(v && do_commit && isCfi, p"cfi_update: isBr(${isBr}) pc(${Hexadecimal(pc)}) " +
    p"taken(${isTaken}) mispred(${misPred}) cycle($predCycle) hist(${histPtr.value}) " +
    p"startAddr(${Hexadecimal(commit_pc_bundle.startAddr)}) AddIntoHist(${addIntoHist}) " +
    p"brInEntry(${inFtbEntry}) brIdx(${brIdx}) target(${Hexadecimal(target)})\n")
  }

  
  val arbiter_post = Wire(Vec(FtqSize, Bool()))
  arbiter_post.zipWithIndex.map{ case (a, i) => a := (if (i == FtqSize - 1) {arbiter_flag(0.U)} else {arbiter_flag((i + 1).U)})}
  

  val enq = io.fromBpu.resp
  val perf_redirect = backendRedirect

  XSPerfAccumulate("entry", validEntries)
  XSPerfAccumulate("bpu_to_ftq_stall", enq.valid && !enq.ready)
  XSPerfAccumulate("mispredictRedirect", perf_redirect.valid && RedirectLevel.flushAfter === perf_redirect.bits.level)
  XSPerfAccumulate("replayRedirect", perf_redirect.valid && RedirectLevel.flushItself(perf_redirect.bits.level))
  XSPerfAccumulate("predecodeRedirect", fromIfuRedirect.valid)

  XSPerfAccumulate("to_ifu_bubble", io.toIfu.req.ready && !io.toIfu.req.valid)

  XSPerfAccumulate("to_ifu_stall", io.toIfu.req.valid && !io.toIfu.req.ready)
  XSPerfAccumulate("from_bpu_real_bubble", !enq.valid && enq.ready && allowBpuIn)
  XSPerfAccumulate("bpu_to_ifu_bubble", bpuPtr === ifuPtr)
  val lc_exit_early = WireInit(perf_redirect.valid && arbiter_flag(perf_redirect.bits.ftqIdx.value) && !arbiter_post(perf_redirect.bits.ftqIdx.value))
  val lc_exit_late = WireInit(perf_redirect.valid && arbiter_flag(perf_redirect.bits.ftqIdx.value) && arbiter_post(perf_redirect.bits.ftqIdx.value))
  XSPerfAccumulate("lc_exit_late_redirect", lc_exit_late)
  XSPerfAccumulate("lc_exit_early_redirect", lc_exit_early)
  XSPerfAccumulate("lc_all_takeover", loopMainCache.io.toFtqRedirect.valid)
  XSPerfAccumulate("lc_bypass_commit", do_commit && isBypass(do_commit_ptr.value))
  XSPerfAccumulate("lc_bypass_commit_double", do_commit && isBypass(do_commit_ptr.value) && isDouble(do_commit_ptr.value))
  XSPerfAccumulate("lc_bypass_commit_single", do_commit && isBypass(do_commit_ptr.value) && !isDouble(do_commit_ptr.value))
  XSPerfAccumulate("lc_bypass_commit_single_redirected", do_commit && isBypass(do_commit_ptr.value) && !isDouble(do_commit_ptr.value) && commit_mispredict.asUInt.orR())
  XSPerfAccumulate("lc_bypass_commit_double_redirected", do_commit && isBypass(do_commit_ptr.value) && isDouble(do_commit_ptr.value) && commit_mispredict.asUInt.orR())
  XSPerfAccumulate("lc_bypass_redirect", fromBackendRedirect.valid && isBypass(fromBackendRedirect.bits.ftqIdx.value))
  XSPerfAccumulate("lc_bypass_redirect_double", fromBackendRedirect.valid && isBypass(fromBackendRedirect.bits.ftqIdx.value) && isDouble(fromBackendRedirect.bits.ftqIdx.value))
  XSPerfAccumulate("lc_bypass_redirect_single", fromBackendRedirect.valid && isBypass(fromBackendRedirect.bits.ftqIdx.value) && !isDouble(fromBackendRedirect.bits.ftqIdx.value))

  XSDebug(fromBackendRedirect.valid && isBypass(fromBackendRedirect.bits.ftqIdx.value) , p"BypassMispred: PC: ${Hexadecimal(fromBackendRedirect.bits.cfiUpdate.pc)} Right: ${fromBackendRedirect.bits.cfiUpdate} pred remainCnt: ${bypassCnt(fromBackendRedirect.bits.ftqIdx.value)} pred double: ${isDouble(fromBackendRedirect.bits.ftqIdx.value)}\n")

  val from_bpu = io.fromBpu.resp.bits
  def in_entry_len_map_gen(resp: BpuToFtqBundle)(stage: String) = {
    val entry_len = (resp.last_stage_ftb_entry.getFallThrough(resp.s3.pc(dupForFtq)) - resp.s3.pc(dupForFtq)) >> instOffsetBits
    val entry_len_recording_vec = (1 to PredictWidth+1).map(i => entry_len === i.U)
    val entry_len_map = (1 to PredictWidth+1).map(i =>
      f"${stage}_ftb_entry_len_$i" -> (entry_len_recording_vec(i-1) && resp.s3.valid(dupForFtq))
    ).foldLeft(Map[String, UInt]())(_+_)
    entry_len_map
  }
  val s3_entry_len_map = in_entry_len_map_gen(from_bpu)("s3")

  val to_ifu = io.toIfu.req.bits



  val commit_num_inst_recording_vec = (1 to PredictWidth).map(i => PopCount(commit_inst_mask) === i.U)
  val commit_num_inst_map = (1 to PredictWidth).map(i =>
    f"commit_num_inst_$i" -> (commit_num_inst_recording_vec(i-1) && do_commit)
  ).foldLeft(Map[String, UInt]())(_+_)



  val commit_jal_mask  = UIntToOH(commit_pd.jmpOffset) & Fill(PredictWidth, commit_pd.hasJal.asTypeOf(UInt(1.W)))
  val commit_jalr_mask = UIntToOH(commit_pd.jmpOffset) & Fill(PredictWidth, commit_pd.hasJalr.asTypeOf(UInt(1.W)))
  val commit_call_mask = UIntToOH(commit_pd.jmpOffset) & Fill(PredictWidth, commit_pd.hasCall.asTypeOf(UInt(1.W)))
  val commit_ret_mask  = UIntToOH(commit_pd.jmpOffset) & Fill(PredictWidth, commit_pd.hasRet.asTypeOf(UInt(1.W)))


  val mbpBRights = mbpRights & commit_br_mask
  val mbpJRights = mbpRights & commit_jal_mask
  val mbpIRights = mbpRights & commit_jalr_mask
  val mbpCRights = mbpRights & commit_call_mask
  val mbpRRights = mbpRights & commit_ret_mask

  val mbpBWrongs = mbpWrongs & commit_br_mask
  val mbpJWrongs = mbpWrongs & commit_jal_mask
  val mbpIWrongs = mbpWrongs & commit_jalr_mask
  val mbpCWrongs = mbpWrongs & commit_call_mask
  val mbpRWrongs = mbpWrongs & commit_ret_mask

  val commit_pred_stage = RegNext(pred_stage(commPtr.value))

  def pred_stage_map(src: UInt, name: String) = {
    (0 until numBpStages).map(i =>
      f"${name}_stage_${i+1}" -> PopCount(src.asBools.map(_ && commit_pred_stage === BP_STAGES(i)))
    ).foldLeft(Map[String, UInt]())(_+_)
  }

  val mispred_stage_map      = pred_stage_map(mbpWrongs,  "mispredict")
  val br_mispred_stage_map   = pred_stage_map(mbpBWrongs, "br_mispredict")
  val jalr_mispred_stage_map = pred_stage_map(mbpIWrongs, "jalr_mispredict")
  val correct_stage_map      = pred_stage_map(mbpRights,  "correct")
  val br_correct_stage_map   = pred_stage_map(mbpBRights, "br_correct")
  val jalr_correct_stage_map = pred_stage_map(mbpIRights, "jalr_correct")

  val update_valid = io.toBpu.update.valid
  def u(cond: Bool) = update_valid && cond
  val ftb_false_hit = u(update.false_hit)
  // assert(!ftb_false_hit)
  val ftb_hit = u(commit_hit === h_hit)

  val ftb_new_entry = u(ftbEntryGen.is_init_entry)
  val ftb_new_entry_only_br = ftb_new_entry && !update_ftb_entry.jmpValid
  val ftb_new_entry_only_jmp = ftb_new_entry && !update_ftb_entry.brValids(0)
  val ftb_new_entry_has_br_and_jmp = ftb_new_entry && update_ftb_entry.brValids(0) && update_ftb_entry.jmpValid

  val ftb_old_entry = u(ftbEntryGen.is_old_entry)

  val ftb_modified_entry = u(ftbEntryGen.is_new_br || ftbEntryGen.is_jalr_target_modified || ftbEntryGen.is_always_taken_modified)
  val ftb_modified_entry_new_br = u(ftbEntryGen.is_new_br)
  val ftb_modified_entry_jalr_target_modified = u(ftbEntryGen.is_jalr_target_modified)
  val ftb_modified_entry_br_full = ftb_modified_entry && ftbEntryGen.is_br_full
  val ftb_modified_entry_always_taken = ftb_modified_entry && ftbEntryGen.is_always_taken_modified

  val ftb_entry_len = (ftbEntryGen.new_entry.getFallThrough(update.pc) - update.pc) >> instOffsetBits
  val ftb_entry_len_recording_vec = (1 to PredictWidth+1).map(i => ftb_entry_len === i.U)
  val ftb_init_entry_len_map = (1 to PredictWidth+1).map(i =>
    f"ftb_init_entry_len_$i" -> (ftb_entry_len_recording_vec(i-1) && ftb_new_entry)
  ).foldLeft(Map[String, UInt]())(_+_)
  val ftb_modified_entry_len_map = (1 to PredictWidth+1).map(i =>
    f"ftb_modified_entry_len_$i" -> (ftb_entry_len_recording_vec(i-1) && ftb_modified_entry)
  ).foldLeft(Map[String, UInt]())(_+_)

  val ftq_occupancy_map = (0 to FtqSize).map(i =>
    f"ftq_has_entry_$i" ->( validEntries === i.U)
  ).foldLeft(Map[String, UInt]())(_+_)

  val perfCountsMap = Map(
    "BpInstr" -> PopCount(mbpInstrs),
    "BpBInstr" -> PopCount(mbpBRights | mbpBWrongs),
    "BpRight"  -> PopCount(mbpRights),
    "BpWrong"  -> PopCount(mbpWrongs),
    "BpBRight" -> PopCount(mbpBRights),
    "BpBWrong" -> PopCount(mbpBWrongs),
    "BpJRight" -> PopCount(mbpJRights),
    "BpJWrong" -> PopCount(mbpJWrongs),
    "BpIRight" -> PopCount(mbpIRights),
    "BpIWrong" -> PopCount(mbpIWrongs),
    "BpCRight" -> PopCount(mbpCRights),
    "BpCWrong" -> PopCount(mbpCWrongs),
    "BpRRight" -> PopCount(mbpRRights),
    "BpRWrong" -> PopCount(mbpRWrongs),

    "ftb_false_hit"                -> PopCount(ftb_false_hit),
    "ftb_hit"                      -> PopCount(ftb_hit),
    "ftb_new_entry"                -> PopCount(ftb_new_entry),
    "ftb_new_entry_only_br"        -> PopCount(ftb_new_entry_only_br),
    "ftb_new_entry_only_jmp"       -> PopCount(ftb_new_entry_only_jmp),
    "ftb_new_entry_has_br_and_jmp" -> PopCount(ftb_new_entry_has_br_and_jmp),
    "ftb_old_entry"                -> PopCount(ftb_old_entry),
    "ftb_modified_entry"           -> PopCount(ftb_modified_entry),
    "ftb_modified_entry_new_br"    -> PopCount(ftb_modified_entry_new_br),
    "ftb_jalr_target_modified"     -> PopCount(ftb_modified_entry_jalr_target_modified),
    "ftb_modified_entry_br_full"   -> PopCount(ftb_modified_entry_br_full),
    "ftb_modified_entry_always_taken" -> PopCount(ftb_modified_entry_always_taken)
  ) ++ ftb_init_entry_len_map ++ ftb_modified_entry_len_map ++
  s3_entry_len_map ++ commit_num_inst_map ++ ftq_occupancy_map ++
  mispred_stage_map ++ br_mispred_stage_map ++ jalr_mispred_stage_map ++
  correct_stage_map ++ br_correct_stage_map ++ jalr_correct_stage_map

  for((key, value) <- perfCountsMap) {
    XSPerfAccumulate(key, value)
  }

  // --------------------------- Debug --------------------------------
  // XSDebug(enq_fire, p"enq! " + io.fromBpu.resp.bits.toPrintable)
  XSDebug(io.toIfu.req.fire, p"fire to ifu " + io.toIfu.req.bits.toPrintable)
  XSDebug(do_commit, p"deq! [ptr] $do_commit_ptr\n")
  XSDebug(true.B, p"[bpuPtr] $bpuPtr, [ifuPtr] $ifuPtr, [ifuWbPtr] $ifuWbPtr [commPtr] $commPtr\n")
  XSDebug(true.B, p"[in] v:${io.fromBpu.resp.valid} r:${io.fromBpu.resp.ready} " +
    p"[out] v:${io.toIfu.req.valid} r:${io.toIfu.req.ready}\n")
  XSDebug(do_commit, p"[deq info] cfiIndex: $commit_cfi, $commit_pc_bundle, target: ${Hexadecimal(commit_target)}\n")

  //   def ubtbCheck(commit: FtqEntry, predAns: Seq[PredictorAnswer], isWrong: Bool) = {
  //     commit.valids.zip(commit.pd).zip(predAns).zip(commit.takens).map {
  //       case (((valid, pd), ans), taken) =>
  //       Mux(valid && pd.isBr,
  //         isWrong ^ Mux(ans.hit.asBool,
  //           Mux(ans.taken.asBool, taken && ans.target === commitEntry.target,
  //           !taken),
  //         !taken),
  //       false.B)
  //     }
  //   }

  //   def btbCheck(commit: FtqEntry, predAns: Seq[PredictorAnswer], isWrong: Bool) = {
  //     commit.valids.zip(commit.pd).zip(predAns).zip(commit.takens).map {
  //       case (((valid, pd), ans), taken) =>
  //       Mux(valid && pd.isBr,
  //         isWrong ^ Mux(ans.hit.asBool,
  //           Mux(ans.taken.asBool, taken && ans.target === commitEntry.target,
  //           !taken),
  //         !taken),
  //       false.B)
  //     }
  //   }

  //   def tageCheck(commit: FtqEntry, predAns: Seq[PredictorAnswer], isWrong: Bool) = {
  //     commit.valids.zip(commit.pd).zip(predAns).zip(commit.takens).map {
  //       case (((valid, pd), ans), taken) =>
  //       Mux(valid && pd.isBr,
  //         isWrong ^ (ans.taken.asBool === taken),
  //       false.B)
  //     }
  //   }

  //   def loopCheck(commit: FtqEntry, predAns: Seq[PredictorAnswer], isWrong: Bool) = {
  //     commit.valids.zip(commit.pd).zip(predAns).zip(commit.takens).map {
  //       case (((valid, pd), ans), taken) =>
  //       Mux(valid && (pd.isBr) && ans.hit.asBool,
  //         isWrong ^ (!taken),
  //           false.B)
  //     }
  //   }

  //   def rasCheck(commit: FtqEntry, predAns: Seq[PredictorAnswer], isWrong: Bool) = {
  //     commit.valids.zip(commit.pd).zip(predAns).zip(commit.takens).map {
  //       case (((valid, pd), ans), taken) =>
  //       Mux(valid && pd.isRet.asBool /*&& taken*/ && ans.hit.asBool,
  //         isWrong ^ (ans.target === commitEntry.target),
  //           false.B)
  //     }
  //   }

  //   val ubtbRights = ubtbCheck(commitEntry, commitEntry.metas.map(_.ubtbAns), false.B)
  //   val ubtbWrongs = ubtbCheck(commitEntry, commitEntry.metas.map(_.ubtbAns), true.B)
  //   // btb and ubtb pred jal and jalr as well
  //   val btbRights = btbCheck(commitEntry, commitEntry.metas.map(_.btbAns), false.B)
  //   val btbWrongs = btbCheck(commitEntry, commitEntry.metas.map(_.btbAns), true.B)
  //   val tageRights = tageCheck(commitEntry, commitEntry.metas.map(_.tageAns), false.B)
  //   val tageWrongs = tageCheck(commitEntry, commitEntry.metas.map(_.tageAns), true.B)

  //   val loopRights = loopCheck(commitEntry, commitEntry.metas.map(_.loopAns), false.B)
  //   val loopWrongs = loopCheck(commitEntry, commitEntry.metas.map(_.loopAns), true.B)

  //   val rasRights = rasCheck(commitEntry, commitEntry.metas.map(_.rasAns), false.B)
  //   val rasWrongs = rasCheck(commitEntry, commitEntry.metas.map(_.rasAns), true.B)

  val perfEvents = Seq(
    ("bpu_s2_redirect        ", bpu_s2_redirect                                                             ),
    ("bpu_s3_redirect        ", bpu_s3_redirect                                                             ),
    ("bpu_to_ftq_stall       ", enq.valid && ~enq.ready                                                     ),
    ("mispredictRedirect     ", perf_redirect.valid && RedirectLevel.flushAfter === perf_redirect.bits.level),
    ("replayRedirect         ", perf_redirect.valid && RedirectLevel.flushItself(perf_redirect.bits.level)  ),
    ("predecodeRedirect      ", fromIfuRedirect.valid                                                       ),
    ("to_ifu_bubble          ", io.toIfu.req.ready && !io.toIfu.req.valid                                   ),
    ("from_bpu_real_bubble   ", !enq.valid && enq.ready && allowBpuIn                                       ),
    ("BpInstr                ", PopCount(mbpInstrs)                                                         ),
    ("BpBInstr               ", PopCount(mbpBRights | mbpBWrongs)                                           ),
    ("BpRight                ", PopCount(mbpRights)                                                         ),
    ("BpWrong                ", PopCount(mbpWrongs)                                                         ),
    ("BpBRight               ", PopCount(mbpBRights)                                                        ),
    ("BpBWrong               ", PopCount(mbpBWrongs)                                                        ),
    ("BpJRight               ", PopCount(mbpJRights)                                                        ),
    ("BpJWrong               ", PopCount(mbpJWrongs)                                                        ),
    ("BpIRight               ", PopCount(mbpIRights)                                                        ),
    ("BpIWrong               ", PopCount(mbpIWrongs)                                                        ),
    ("BpCRight               ", PopCount(mbpCRights)                                                        ),
    ("BpCWrong               ", PopCount(mbpCWrongs)                                                        ),
    ("BpRRight               ", PopCount(mbpRRights)                                                        ),
    ("BpRWrong               ", PopCount(mbpRWrongs)                                                        ),
    ("ftb_false_hit          ", PopCount(ftb_false_hit)                                                     ),
    ("ftb_hit                ", PopCount(ftb_hit)                                                           ),
  )
  generatePerfEvent()
}
