module CLZ(
  input  [63:0] io_in,
  output [5:0]  io_out
);
  wire [5:0] _io_out_T_64 = io_in[1] ? 6'h3e : 6'h3f; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_65 = io_in[2] ? 6'h3d : _io_out_T_64; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_66 = io_in[3] ? 6'h3c : _io_out_T_65; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_67 = io_in[4] ? 6'h3b : _io_out_T_66; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_68 = io_in[5] ? 6'h3a : _io_out_T_67; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_69 = io_in[6] ? 6'h39 : _io_out_T_68; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_70 = io_in[7] ? 6'h38 : _io_out_T_69; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_71 = io_in[8] ? 6'h37 : _io_out_T_70; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_72 = io_in[9] ? 6'h36 : _io_out_T_71; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_73 = io_in[10] ? 6'h35 : _io_out_T_72; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_74 = io_in[11] ? 6'h34 : _io_out_T_73; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_75 = io_in[12] ? 6'h33 : _io_out_T_74; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_76 = io_in[13] ? 6'h32 : _io_out_T_75; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_77 = io_in[14] ? 6'h31 : _io_out_T_76; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_78 = io_in[15] ? 6'h30 : _io_out_T_77; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_79 = io_in[16] ? 6'h2f : _io_out_T_78; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_80 = io_in[17] ? 6'h2e : _io_out_T_79; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_81 = io_in[18] ? 6'h2d : _io_out_T_80; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_82 = io_in[19] ? 6'h2c : _io_out_T_81; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_83 = io_in[20] ? 6'h2b : _io_out_T_82; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_84 = io_in[21] ? 6'h2a : _io_out_T_83; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_85 = io_in[22] ? 6'h29 : _io_out_T_84; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_86 = io_in[23] ? 6'h28 : _io_out_T_85; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_87 = io_in[24] ? 6'h27 : _io_out_T_86; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_88 = io_in[25] ? 6'h26 : _io_out_T_87; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_89 = io_in[26] ? 6'h25 : _io_out_T_88; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_90 = io_in[27] ? 6'h24 : _io_out_T_89; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_91 = io_in[28] ? 6'h23 : _io_out_T_90; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_92 = io_in[29] ? 6'h22 : _io_out_T_91; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_93 = io_in[30] ? 6'h21 : _io_out_T_92; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_94 = io_in[31] ? 6'h20 : _io_out_T_93; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_95 = io_in[32] ? 6'h1f : _io_out_T_94; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_96 = io_in[33] ? 6'h1e : _io_out_T_95; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_97 = io_in[34] ? 6'h1d : _io_out_T_96; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_98 = io_in[35] ? 6'h1c : _io_out_T_97; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_99 = io_in[36] ? 6'h1b : _io_out_T_98; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_100 = io_in[37] ? 6'h1a : _io_out_T_99; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_101 = io_in[38] ? 6'h19 : _io_out_T_100; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_102 = io_in[39] ? 6'h18 : _io_out_T_101; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_103 = io_in[40] ? 6'h17 : _io_out_T_102; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_104 = io_in[41] ? 6'h16 : _io_out_T_103; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_105 = io_in[42] ? 6'h15 : _io_out_T_104; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_106 = io_in[43] ? 6'h14 : _io_out_T_105; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_107 = io_in[44] ? 6'h13 : _io_out_T_106; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_108 = io_in[45] ? 6'h12 : _io_out_T_107; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_109 = io_in[46] ? 6'h11 : _io_out_T_108; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_110 = io_in[47] ? 6'h10 : _io_out_T_109; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_111 = io_in[48] ? 6'hf : _io_out_T_110; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_112 = io_in[49] ? 6'he : _io_out_T_111; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_113 = io_in[50] ? 6'hd : _io_out_T_112; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_114 = io_in[51] ? 6'hc : _io_out_T_113; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_115 = io_in[52] ? 6'hb : _io_out_T_114; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_116 = io_in[53] ? 6'ha : _io_out_T_115; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_117 = io_in[54] ? 6'h9 : _io_out_T_116; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_118 = io_in[55] ? 6'h8 : _io_out_T_117; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_119 = io_in[56] ? 6'h7 : _io_out_T_118; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_120 = io_in[57] ? 6'h6 : _io_out_T_119; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_121 = io_in[58] ? 6'h5 : _io_out_T_120; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_122 = io_in[59] ? 6'h4 : _io_out_T_121; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_123 = io_in[60] ? 6'h3 : _io_out_T_122; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_124 = io_in[61] ? 6'h2 : _io_out_T_123; // @[Mux.scala 47:70]
  wire [5:0] _io_out_T_125 = io_in[62] ? 6'h1 : _io_out_T_124; // @[Mux.scala 47:70]
  assign io_out = io_in[63] ? 6'h0 : _io_out_T_125; // @[Mux.scala 47:70]
endmodule
