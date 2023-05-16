module CSA3_2_1982(
  input  [67:0] io_in_0,
  input  [67:0] io_in_1,
  input  [67:0] io_in_2,
  output [67:0] io_out_0,
  output [67:0] io_out_1
);
  wire  a = io_in_0[0]; // @[CSA.scala 43:32]
  wire  b = io_in_1[0]; // @[CSA.scala 43:45]
  wire  cin = io_in_2[0]; // @[CSA.scala 43:58]
  wire  a_xor_b = a ^ b; // @[CSA.scala 44:21]
  wire  a_and_b = a & b; // @[CSA.scala 45:21]
  wire  sum = a_xor_b ^ cin; // @[CSA.scala 46:23]
  wire  cout = a_and_b | a_xor_b & cin; // @[CSA.scala 47:24]
  wire [1:0] temp_0 = {cout,sum}; // @[Cat.scala 31:58]
  wire  a_1 = io_in_0[1]; // @[CSA.scala 43:32]
  wire  b_1 = io_in_1[1]; // @[CSA.scala 43:45]
  wire  cin_1 = io_in_2[1]; // @[CSA.scala 43:58]
  wire  a_xor_b1 = a_1 ^ b_1; // @[CSA.scala 44:21]
  wire  a_and_b1 = a_1 & b_1; // @[CSA.scala 45:21]
  wire  sum_1 = a_xor_b1 ^ cin_1; // @[CSA.scala 46:23]
  wire  cout_1 = a_and_b1 | a_xor_b1 & cin_1; // @[CSA.scala 47:24]
  wire [1:0] temp_1 = {cout_1,sum_1}; // @[Cat.scala 31:58]
  wire  a_2 = io_in_0[2]; // @[CSA.scala 43:32]
  wire  b_2 = io_in_1[2]; // @[CSA.scala 43:45]
  wire  cin_2 = io_in_2[2]; // @[CSA.scala 43:58]
  wire  a_xor_b2 = a_2 ^ b_2; // @[CSA.scala 44:21]
  wire  a_and_b2 = a_2 & b_2; // @[CSA.scala 45:21]
  wire  sum_2 = a_xor_b2 ^ cin_2; // @[CSA.scala 46:23]
  wire  cout_2 = a_and_b2 | a_xor_b2 & cin_2; // @[CSA.scala 47:24]
  wire [1:0] temp_2 = {cout_2,sum_2}; // @[Cat.scala 31:58]
  wire  a_3 = io_in_0[3]; // @[CSA.scala 43:32]
  wire  b_3 = io_in_1[3]; // @[CSA.scala 43:45]
  wire  cin_3 = io_in_2[3]; // @[CSA.scala 43:58]
  wire  a_xor_b3 = a_3 ^ b_3; // @[CSA.scala 44:21]
  wire  a_and_b3 = a_3 & b_3; // @[CSA.scala 45:21]
  wire  sum_3 = a_xor_b3 ^ cin_3; // @[CSA.scala 46:23]
  wire  cout_3 = a_and_b3 | a_xor_b3 & cin_3; // @[CSA.scala 47:24]
  wire [1:0] temp_3 = {cout_3,sum_3}; // @[Cat.scala 31:58]
  wire  a_4 = io_in_0[4]; // @[CSA.scala 43:32]
  wire  b_4 = io_in_1[4]; // @[CSA.scala 43:45]
  wire  cin_4 = io_in_2[4]; // @[CSA.scala 43:58]
  wire  a_xor_b4 = a_4 ^ b_4; // @[CSA.scala 44:21]
  wire  a_and_b4 = a_4 & b_4; // @[CSA.scala 45:21]
  wire  sum_4 = a_xor_b4 ^ cin_4; // @[CSA.scala 46:23]
  wire  cout_4 = a_and_b4 | a_xor_b4 & cin_4; // @[CSA.scala 47:24]
  wire [1:0] temp_4 = {cout_4,sum_4}; // @[Cat.scala 31:58]
  wire  a_5 = io_in_0[5]; // @[CSA.scala 43:32]
  wire  b_5 = io_in_1[5]; // @[CSA.scala 43:45]
  wire  cin_5 = io_in_2[5]; // @[CSA.scala 43:58]
  wire  a_xor_b5 = a_5 ^ b_5; // @[CSA.scala 44:21]
  wire  a_and_b5 = a_5 & b_5; // @[CSA.scala 45:21]
  wire  sum_5 = a_xor_b5 ^ cin_5; // @[CSA.scala 46:23]
  wire  cout_5 = a_and_b5 | a_xor_b5 & cin_5; // @[CSA.scala 47:24]
  wire [1:0] temp_5 = {cout_5,sum_5}; // @[Cat.scala 31:58]
  wire  a_6 = io_in_0[6]; // @[CSA.scala 43:32]
  wire  b_6 = io_in_1[6]; // @[CSA.scala 43:45]
  wire  cin_6 = io_in_2[6]; // @[CSA.scala 43:58]
  wire  a_xor_b6 = a_6 ^ b_6; // @[CSA.scala 44:21]
  wire  a_and_b6 = a_6 & b_6; // @[CSA.scala 45:21]
  wire  sum_6 = a_xor_b6 ^ cin_6; // @[CSA.scala 46:23]
  wire  cout_6 = a_and_b6 | a_xor_b6 & cin_6; // @[CSA.scala 47:24]
  wire [1:0] temp_6 = {cout_6,sum_6}; // @[Cat.scala 31:58]
  wire  a_7 = io_in_0[7]; // @[CSA.scala 43:32]
  wire  b_7 = io_in_1[7]; // @[CSA.scala 43:45]
  wire  cin_7 = io_in_2[7]; // @[CSA.scala 43:58]
  wire  a_xor_b7 = a_7 ^ b_7; // @[CSA.scala 44:21]
  wire  a_and_b7 = a_7 & b_7; // @[CSA.scala 45:21]
  wire  sum_7 = a_xor_b7 ^ cin_7; // @[CSA.scala 46:23]
  wire  cout_7 = a_and_b7 | a_xor_b7 & cin_7; // @[CSA.scala 47:24]
  wire [1:0] temp_7 = {cout_7,sum_7}; // @[Cat.scala 31:58]
  wire  a_8 = io_in_0[8]; // @[CSA.scala 43:32]
  wire  b_8 = io_in_1[8]; // @[CSA.scala 43:45]
  wire  cin_8 = io_in_2[8]; // @[CSA.scala 43:58]
  wire  a_xor_b8 = a_8 ^ b_8; // @[CSA.scala 44:21]
  wire  a_and_b8 = a_8 & b_8; // @[CSA.scala 45:21]
  wire  sum_8 = a_xor_b8 ^ cin_8; // @[CSA.scala 46:23]
  wire  cout_8 = a_and_b8 | a_xor_b8 & cin_8; // @[CSA.scala 47:24]
  wire [1:0] temp_8 = {cout_8,sum_8}; // @[Cat.scala 31:58]
  wire  a_9 = io_in_0[9]; // @[CSA.scala 43:32]
  wire  b_9 = io_in_1[9]; // @[CSA.scala 43:45]
  wire  cin_9 = io_in_2[9]; // @[CSA.scala 43:58]
  wire  a_xor_b9 = a_9 ^ b_9; // @[CSA.scala 44:21]
  wire  a_and_b9 = a_9 & b_9; // @[CSA.scala 45:21]
  wire  sum_9 = a_xor_b9 ^ cin_9; // @[CSA.scala 46:23]
  wire  cout_9 = a_and_b9 | a_xor_b9 & cin_9; // @[CSA.scala 47:24]
  wire [1:0] temp_9 = {cout_9,sum_9}; // @[Cat.scala 31:58]
  wire  a_10 = io_in_0[10]; // @[CSA.scala 43:32]
  wire  b_10 = io_in_1[10]; // @[CSA.scala 43:45]
  wire  cin_10 = io_in_2[10]; // @[CSA.scala 43:58]
  wire  a_xor_b10 = a_10 ^ b_10; // @[CSA.scala 44:21]
  wire  a_and_b10 = a_10 & b_10; // @[CSA.scala 45:21]
  wire  sum_10 = a_xor_b10 ^ cin_10; // @[CSA.scala 46:23]
  wire  cout_10 = a_and_b10 | a_xor_b10 & cin_10; // @[CSA.scala 47:24]
  wire [1:0] temp_10 = {cout_10,sum_10}; // @[Cat.scala 31:58]
  wire  a_11 = io_in_0[11]; // @[CSA.scala 43:32]
  wire  b_11 = io_in_1[11]; // @[CSA.scala 43:45]
  wire  cin_11 = io_in_2[11]; // @[CSA.scala 43:58]
  wire  a_xor_b11 = a_11 ^ b_11; // @[CSA.scala 44:21]
  wire  a_and_b11 = a_11 & b_11; // @[CSA.scala 45:21]
  wire  sum_11 = a_xor_b11 ^ cin_11; // @[CSA.scala 46:23]
  wire  cout_11 = a_and_b11 | a_xor_b11 & cin_11; // @[CSA.scala 47:24]
  wire [1:0] temp_11 = {cout_11,sum_11}; // @[Cat.scala 31:58]
  wire  a_12 = io_in_0[12]; // @[CSA.scala 43:32]
  wire  b_12 = io_in_1[12]; // @[CSA.scala 43:45]
  wire  cin_12 = io_in_2[12]; // @[CSA.scala 43:58]
  wire  a_xor_b12 = a_12 ^ b_12; // @[CSA.scala 44:21]
  wire  a_and_b12 = a_12 & b_12; // @[CSA.scala 45:21]
  wire  sum_12 = a_xor_b12 ^ cin_12; // @[CSA.scala 46:23]
  wire  cout_12 = a_and_b12 | a_xor_b12 & cin_12; // @[CSA.scala 47:24]
  wire [1:0] temp_12 = {cout_12,sum_12}; // @[Cat.scala 31:58]
  wire  a_13 = io_in_0[13]; // @[CSA.scala 43:32]
  wire  b_13 = io_in_1[13]; // @[CSA.scala 43:45]
  wire  cin_13 = io_in_2[13]; // @[CSA.scala 43:58]
  wire  a_xor_b13 = a_13 ^ b_13; // @[CSA.scala 44:21]
  wire  a_and_b13 = a_13 & b_13; // @[CSA.scala 45:21]
  wire  sum_13 = a_xor_b13 ^ cin_13; // @[CSA.scala 46:23]
  wire  cout_13 = a_and_b13 | a_xor_b13 & cin_13; // @[CSA.scala 47:24]
  wire [1:0] temp_13 = {cout_13,sum_13}; // @[Cat.scala 31:58]
  wire  a_14 = io_in_0[14]; // @[CSA.scala 43:32]
  wire  b_14 = io_in_1[14]; // @[CSA.scala 43:45]
  wire  cin_14 = io_in_2[14]; // @[CSA.scala 43:58]
  wire  a_xor_b14 = a_14 ^ b_14; // @[CSA.scala 44:21]
  wire  a_and_b14 = a_14 & b_14; // @[CSA.scala 45:21]
  wire  sum_14 = a_xor_b14 ^ cin_14; // @[CSA.scala 46:23]
  wire  cout_14 = a_and_b14 | a_xor_b14 & cin_14; // @[CSA.scala 47:24]
  wire [1:0] temp_14 = {cout_14,sum_14}; // @[Cat.scala 31:58]
  wire  a_15 = io_in_0[15]; // @[CSA.scala 43:32]
  wire  b_15 = io_in_1[15]; // @[CSA.scala 43:45]
  wire  cin_15 = io_in_2[15]; // @[CSA.scala 43:58]
  wire  a_xor_b15 = a_15 ^ b_15; // @[CSA.scala 44:21]
  wire  a_and_b15 = a_15 & b_15; // @[CSA.scala 45:21]
  wire  sum_15 = a_xor_b15 ^ cin_15; // @[CSA.scala 46:23]
  wire  cout_15 = a_and_b15 | a_xor_b15 & cin_15; // @[CSA.scala 47:24]
  wire [1:0] temp_15 = {cout_15,sum_15}; // @[Cat.scala 31:58]
  wire  a_16 = io_in_0[16]; // @[CSA.scala 43:32]
  wire  b_16 = io_in_1[16]; // @[CSA.scala 43:45]
  wire  cin_16 = io_in_2[16]; // @[CSA.scala 43:58]
  wire  a_xor_b16 = a_16 ^ b_16; // @[CSA.scala 44:21]
  wire  a_and_b16 = a_16 & b_16; // @[CSA.scala 45:21]
  wire  sum_16 = a_xor_b16 ^ cin_16; // @[CSA.scala 46:23]
  wire  cout_16 = a_and_b16 | a_xor_b16 & cin_16; // @[CSA.scala 47:24]
  wire [1:0] temp_16 = {cout_16,sum_16}; // @[Cat.scala 31:58]
  wire  a_17 = io_in_0[17]; // @[CSA.scala 43:32]
  wire  b_17 = io_in_1[17]; // @[CSA.scala 43:45]
  wire  cin_17 = io_in_2[17]; // @[CSA.scala 43:58]
  wire  a_xor_b17 = a_17 ^ b_17; // @[CSA.scala 44:21]
  wire  a_and_b17 = a_17 & b_17; // @[CSA.scala 45:21]
  wire  sum_17 = a_xor_b17 ^ cin_17; // @[CSA.scala 46:23]
  wire  cout_17 = a_and_b17 | a_xor_b17 & cin_17; // @[CSA.scala 47:24]
  wire [1:0] temp_17 = {cout_17,sum_17}; // @[Cat.scala 31:58]
  wire  a_18 = io_in_0[18]; // @[CSA.scala 43:32]
  wire  b_18 = io_in_1[18]; // @[CSA.scala 43:45]
  wire  cin_18 = io_in_2[18]; // @[CSA.scala 43:58]
  wire  a_xor_b18 = a_18 ^ b_18; // @[CSA.scala 44:21]
  wire  a_and_b18 = a_18 & b_18; // @[CSA.scala 45:21]
  wire  sum_18 = a_xor_b18 ^ cin_18; // @[CSA.scala 46:23]
  wire  cout_18 = a_and_b18 | a_xor_b18 & cin_18; // @[CSA.scala 47:24]
  wire [1:0] temp_18 = {cout_18,sum_18}; // @[Cat.scala 31:58]
  wire  a_19 = io_in_0[19]; // @[CSA.scala 43:32]
  wire  b_19 = io_in_1[19]; // @[CSA.scala 43:45]
  wire  cin_19 = io_in_2[19]; // @[CSA.scala 43:58]
  wire  a_xor_b19 = a_19 ^ b_19; // @[CSA.scala 44:21]
  wire  a_and_b19 = a_19 & b_19; // @[CSA.scala 45:21]
  wire  sum_19 = a_xor_b19 ^ cin_19; // @[CSA.scala 46:23]
  wire  cout_19 = a_and_b19 | a_xor_b19 & cin_19; // @[CSA.scala 47:24]
  wire [1:0] temp_19 = {cout_19,sum_19}; // @[Cat.scala 31:58]
  wire  a_20 = io_in_0[20]; // @[CSA.scala 43:32]
  wire  b_20 = io_in_1[20]; // @[CSA.scala 43:45]
  wire  cin_20 = io_in_2[20]; // @[CSA.scala 43:58]
  wire  a_xor_b20 = a_20 ^ b_20; // @[CSA.scala 44:21]
  wire  a_and_b20 = a_20 & b_20; // @[CSA.scala 45:21]
  wire  sum_20 = a_xor_b20 ^ cin_20; // @[CSA.scala 46:23]
  wire  cout_20 = a_and_b20 | a_xor_b20 & cin_20; // @[CSA.scala 47:24]
  wire [1:0] temp_20 = {cout_20,sum_20}; // @[Cat.scala 31:58]
  wire  a_21 = io_in_0[21]; // @[CSA.scala 43:32]
  wire  b_21 = io_in_1[21]; // @[CSA.scala 43:45]
  wire  cin_21 = io_in_2[21]; // @[CSA.scala 43:58]
  wire  a_xor_b21 = a_21 ^ b_21; // @[CSA.scala 44:21]
  wire  a_and_b21 = a_21 & b_21; // @[CSA.scala 45:21]
  wire  sum_21 = a_xor_b21 ^ cin_21; // @[CSA.scala 46:23]
  wire  cout_21 = a_and_b21 | a_xor_b21 & cin_21; // @[CSA.scala 47:24]
  wire [1:0] temp_21 = {cout_21,sum_21}; // @[Cat.scala 31:58]
  wire  a_22 = io_in_0[22]; // @[CSA.scala 43:32]
  wire  b_22 = io_in_1[22]; // @[CSA.scala 43:45]
  wire  cin_22 = io_in_2[22]; // @[CSA.scala 43:58]
  wire  a_xor_b22 = a_22 ^ b_22; // @[CSA.scala 44:21]
  wire  a_and_b22 = a_22 & b_22; // @[CSA.scala 45:21]
  wire  sum_22 = a_xor_b22 ^ cin_22; // @[CSA.scala 46:23]
  wire  cout_22 = a_and_b22 | a_xor_b22 & cin_22; // @[CSA.scala 47:24]
  wire [1:0] temp_22 = {cout_22,sum_22}; // @[Cat.scala 31:58]
  wire  a_23 = io_in_0[23]; // @[CSA.scala 43:32]
  wire  b_23 = io_in_1[23]; // @[CSA.scala 43:45]
  wire  cin_23 = io_in_2[23]; // @[CSA.scala 43:58]
  wire  a_xor_b23 = a_23 ^ b_23; // @[CSA.scala 44:21]
  wire  a_and_b23 = a_23 & b_23; // @[CSA.scala 45:21]
  wire  sum_23 = a_xor_b23 ^ cin_23; // @[CSA.scala 46:23]
  wire  cout_23 = a_and_b23 | a_xor_b23 & cin_23; // @[CSA.scala 47:24]
  wire [1:0] temp_23 = {cout_23,sum_23}; // @[Cat.scala 31:58]
  wire  a_24 = io_in_0[24]; // @[CSA.scala 43:32]
  wire  b_24 = io_in_1[24]; // @[CSA.scala 43:45]
  wire  cin_24 = io_in_2[24]; // @[CSA.scala 43:58]
  wire  a_xor_b24 = a_24 ^ b_24; // @[CSA.scala 44:21]
  wire  a_and_b24 = a_24 & b_24; // @[CSA.scala 45:21]
  wire  sum_24 = a_xor_b24 ^ cin_24; // @[CSA.scala 46:23]
  wire  cout_24 = a_and_b24 | a_xor_b24 & cin_24; // @[CSA.scala 47:24]
  wire [1:0] temp_24 = {cout_24,sum_24}; // @[Cat.scala 31:58]
  wire  a_25 = io_in_0[25]; // @[CSA.scala 43:32]
  wire  b_25 = io_in_1[25]; // @[CSA.scala 43:45]
  wire  cin_25 = io_in_2[25]; // @[CSA.scala 43:58]
  wire  a_xor_b25 = a_25 ^ b_25; // @[CSA.scala 44:21]
  wire  a_and_b25 = a_25 & b_25; // @[CSA.scala 45:21]
  wire  sum_25 = a_xor_b25 ^ cin_25; // @[CSA.scala 46:23]
  wire  cout_25 = a_and_b25 | a_xor_b25 & cin_25; // @[CSA.scala 47:24]
  wire [1:0] temp_25 = {cout_25,sum_25}; // @[Cat.scala 31:58]
  wire  a_26 = io_in_0[26]; // @[CSA.scala 43:32]
  wire  b_26 = io_in_1[26]; // @[CSA.scala 43:45]
  wire  cin_26 = io_in_2[26]; // @[CSA.scala 43:58]
  wire  a_xor_b26 = a_26 ^ b_26; // @[CSA.scala 44:21]
  wire  a_and_b26 = a_26 & b_26; // @[CSA.scala 45:21]
  wire  sum_26 = a_xor_b26 ^ cin_26; // @[CSA.scala 46:23]
  wire  cout_26 = a_and_b26 | a_xor_b26 & cin_26; // @[CSA.scala 47:24]
  wire [1:0] temp_26 = {cout_26,sum_26}; // @[Cat.scala 31:58]
  wire  a_27 = io_in_0[27]; // @[CSA.scala 43:32]
  wire  b_27 = io_in_1[27]; // @[CSA.scala 43:45]
  wire  cin_27 = io_in_2[27]; // @[CSA.scala 43:58]
  wire  a_xor_b27 = a_27 ^ b_27; // @[CSA.scala 44:21]
  wire  a_and_b27 = a_27 & b_27; // @[CSA.scala 45:21]
  wire  sum_27 = a_xor_b27 ^ cin_27; // @[CSA.scala 46:23]
  wire  cout_27 = a_and_b27 | a_xor_b27 & cin_27; // @[CSA.scala 47:24]
  wire [1:0] temp_27 = {cout_27,sum_27}; // @[Cat.scala 31:58]
  wire  a_28 = io_in_0[28]; // @[CSA.scala 43:32]
  wire  b_28 = io_in_1[28]; // @[CSA.scala 43:45]
  wire  cin_28 = io_in_2[28]; // @[CSA.scala 43:58]
  wire  a_xor_b28 = a_28 ^ b_28; // @[CSA.scala 44:21]
  wire  a_and_b28 = a_28 & b_28; // @[CSA.scala 45:21]
  wire  sum_28 = a_xor_b28 ^ cin_28; // @[CSA.scala 46:23]
  wire  cout_28 = a_and_b28 | a_xor_b28 & cin_28; // @[CSA.scala 47:24]
  wire [1:0] temp_28 = {cout_28,sum_28}; // @[Cat.scala 31:58]
  wire  a_29 = io_in_0[29]; // @[CSA.scala 43:32]
  wire  b_29 = io_in_1[29]; // @[CSA.scala 43:45]
  wire  cin_29 = io_in_2[29]; // @[CSA.scala 43:58]
  wire  a_xor_b29 = a_29 ^ b_29; // @[CSA.scala 44:21]
  wire  a_and_b29 = a_29 & b_29; // @[CSA.scala 45:21]
  wire  sum_29 = a_xor_b29 ^ cin_29; // @[CSA.scala 46:23]
  wire  cout_29 = a_and_b29 | a_xor_b29 & cin_29; // @[CSA.scala 47:24]
  wire [1:0] temp_29 = {cout_29,sum_29}; // @[Cat.scala 31:58]
  wire  a_30 = io_in_0[30]; // @[CSA.scala 43:32]
  wire  b_30 = io_in_1[30]; // @[CSA.scala 43:45]
  wire  cin_30 = io_in_2[30]; // @[CSA.scala 43:58]
  wire  a_xor_b30 = a_30 ^ b_30; // @[CSA.scala 44:21]
  wire  a_and_b30 = a_30 & b_30; // @[CSA.scala 45:21]
  wire  sum_30 = a_xor_b30 ^ cin_30; // @[CSA.scala 46:23]
  wire  cout_30 = a_and_b30 | a_xor_b30 & cin_30; // @[CSA.scala 47:24]
  wire [1:0] temp_30 = {cout_30,sum_30}; // @[Cat.scala 31:58]
  wire  a_31 = io_in_0[31]; // @[CSA.scala 43:32]
  wire  b_31 = io_in_1[31]; // @[CSA.scala 43:45]
  wire  cin_31 = io_in_2[31]; // @[CSA.scala 43:58]
  wire  a_xor_b31 = a_31 ^ b_31; // @[CSA.scala 44:21]
  wire  a_and_b31 = a_31 & b_31; // @[CSA.scala 45:21]
  wire  sum_31 = a_xor_b31 ^ cin_31; // @[CSA.scala 46:23]
  wire  cout_31 = a_and_b31 | a_xor_b31 & cin_31; // @[CSA.scala 47:24]
  wire [1:0] temp_31 = {cout_31,sum_31}; // @[Cat.scala 31:58]
  wire  a_32 = io_in_0[32]; // @[CSA.scala 43:32]
  wire  b_32 = io_in_1[32]; // @[CSA.scala 43:45]
  wire  cin_32 = io_in_2[32]; // @[CSA.scala 43:58]
  wire  a_xor_b32 = a_32 ^ b_32; // @[CSA.scala 44:21]
  wire  a_and_b32 = a_32 & b_32; // @[CSA.scala 45:21]
  wire  sum_32 = a_xor_b32 ^ cin_32; // @[CSA.scala 46:23]
  wire  cout_32 = a_and_b32 | a_xor_b32 & cin_32; // @[CSA.scala 47:24]
  wire [1:0] temp_32 = {cout_32,sum_32}; // @[Cat.scala 31:58]
  wire  a_33 = io_in_0[33]; // @[CSA.scala 43:32]
  wire  b_33 = io_in_1[33]; // @[CSA.scala 43:45]
  wire  cin_33 = io_in_2[33]; // @[CSA.scala 43:58]
  wire  a_xor_b33 = a_33 ^ b_33; // @[CSA.scala 44:21]
  wire  a_and_b33 = a_33 & b_33; // @[CSA.scala 45:21]
  wire  sum_33 = a_xor_b33 ^ cin_33; // @[CSA.scala 46:23]
  wire  cout_33 = a_and_b33 | a_xor_b33 & cin_33; // @[CSA.scala 47:24]
  wire [1:0] temp_33 = {cout_33,sum_33}; // @[Cat.scala 31:58]
  wire  a_34 = io_in_0[34]; // @[CSA.scala 43:32]
  wire  b_34 = io_in_1[34]; // @[CSA.scala 43:45]
  wire  cin_34 = io_in_2[34]; // @[CSA.scala 43:58]
  wire  a_xor_b34 = a_34 ^ b_34; // @[CSA.scala 44:21]
  wire  a_and_b34 = a_34 & b_34; // @[CSA.scala 45:21]
  wire  sum_34 = a_xor_b34 ^ cin_34; // @[CSA.scala 46:23]
  wire  cout_34 = a_and_b34 | a_xor_b34 & cin_34; // @[CSA.scala 47:24]
  wire [1:0] temp_34 = {cout_34,sum_34}; // @[Cat.scala 31:58]
  wire  a_35 = io_in_0[35]; // @[CSA.scala 43:32]
  wire  b_35 = io_in_1[35]; // @[CSA.scala 43:45]
  wire  cin_35 = io_in_2[35]; // @[CSA.scala 43:58]
  wire  a_xor_b35 = a_35 ^ b_35; // @[CSA.scala 44:21]
  wire  a_and_b35 = a_35 & b_35; // @[CSA.scala 45:21]
  wire  sum_35 = a_xor_b35 ^ cin_35; // @[CSA.scala 46:23]
  wire  cout_35 = a_and_b35 | a_xor_b35 & cin_35; // @[CSA.scala 47:24]
  wire [1:0] temp_35 = {cout_35,sum_35}; // @[Cat.scala 31:58]
  wire  a_36 = io_in_0[36]; // @[CSA.scala 43:32]
  wire  b_36 = io_in_1[36]; // @[CSA.scala 43:45]
  wire  cin_36 = io_in_2[36]; // @[CSA.scala 43:58]
  wire  a_xor_b36 = a_36 ^ b_36; // @[CSA.scala 44:21]
  wire  a_and_b36 = a_36 & b_36; // @[CSA.scala 45:21]
  wire  sum_36 = a_xor_b36 ^ cin_36; // @[CSA.scala 46:23]
  wire  cout_36 = a_and_b36 | a_xor_b36 & cin_36; // @[CSA.scala 47:24]
  wire [1:0] temp_36 = {cout_36,sum_36}; // @[Cat.scala 31:58]
  wire  a_37 = io_in_0[37]; // @[CSA.scala 43:32]
  wire  b_37 = io_in_1[37]; // @[CSA.scala 43:45]
  wire  cin_37 = io_in_2[37]; // @[CSA.scala 43:58]
  wire  a_xor_b37 = a_37 ^ b_37; // @[CSA.scala 44:21]
  wire  a_and_b37 = a_37 & b_37; // @[CSA.scala 45:21]
  wire  sum_37 = a_xor_b37 ^ cin_37; // @[CSA.scala 46:23]
  wire  cout_37 = a_and_b37 | a_xor_b37 & cin_37; // @[CSA.scala 47:24]
  wire [1:0] temp_37 = {cout_37,sum_37}; // @[Cat.scala 31:58]
  wire  a_38 = io_in_0[38]; // @[CSA.scala 43:32]
  wire  b_38 = io_in_1[38]; // @[CSA.scala 43:45]
  wire  cin_38 = io_in_2[38]; // @[CSA.scala 43:58]
  wire  a_xor_b38 = a_38 ^ b_38; // @[CSA.scala 44:21]
  wire  a_and_b38 = a_38 & b_38; // @[CSA.scala 45:21]
  wire  sum_38 = a_xor_b38 ^ cin_38; // @[CSA.scala 46:23]
  wire  cout_38 = a_and_b38 | a_xor_b38 & cin_38; // @[CSA.scala 47:24]
  wire [1:0] temp_38 = {cout_38,sum_38}; // @[Cat.scala 31:58]
  wire  a_39 = io_in_0[39]; // @[CSA.scala 43:32]
  wire  b_39 = io_in_1[39]; // @[CSA.scala 43:45]
  wire  cin_39 = io_in_2[39]; // @[CSA.scala 43:58]
  wire  a_xor_b39 = a_39 ^ b_39; // @[CSA.scala 44:21]
  wire  a_and_b39 = a_39 & b_39; // @[CSA.scala 45:21]
  wire  sum_39 = a_xor_b39 ^ cin_39; // @[CSA.scala 46:23]
  wire  cout_39 = a_and_b39 | a_xor_b39 & cin_39; // @[CSA.scala 47:24]
  wire [1:0] temp_39 = {cout_39,sum_39}; // @[Cat.scala 31:58]
  wire  a_40 = io_in_0[40]; // @[CSA.scala 43:32]
  wire  b_40 = io_in_1[40]; // @[CSA.scala 43:45]
  wire  cin_40 = io_in_2[40]; // @[CSA.scala 43:58]
  wire  a_xor_b40 = a_40 ^ b_40; // @[CSA.scala 44:21]
  wire  a_and_b40 = a_40 & b_40; // @[CSA.scala 45:21]
  wire  sum_40 = a_xor_b40 ^ cin_40; // @[CSA.scala 46:23]
  wire  cout_40 = a_and_b40 | a_xor_b40 & cin_40; // @[CSA.scala 47:24]
  wire [1:0] temp_40 = {cout_40,sum_40}; // @[Cat.scala 31:58]
  wire  a_41 = io_in_0[41]; // @[CSA.scala 43:32]
  wire  b_41 = io_in_1[41]; // @[CSA.scala 43:45]
  wire  cin_41 = io_in_2[41]; // @[CSA.scala 43:58]
  wire  a_xor_b41 = a_41 ^ b_41; // @[CSA.scala 44:21]
  wire  a_and_b41 = a_41 & b_41; // @[CSA.scala 45:21]
  wire  sum_41 = a_xor_b41 ^ cin_41; // @[CSA.scala 46:23]
  wire  cout_41 = a_and_b41 | a_xor_b41 & cin_41; // @[CSA.scala 47:24]
  wire [1:0] temp_41 = {cout_41,sum_41}; // @[Cat.scala 31:58]
  wire  a_42 = io_in_0[42]; // @[CSA.scala 43:32]
  wire  b_42 = io_in_1[42]; // @[CSA.scala 43:45]
  wire  cin_42 = io_in_2[42]; // @[CSA.scala 43:58]
  wire  a_xor_b42 = a_42 ^ b_42; // @[CSA.scala 44:21]
  wire  a_and_b42 = a_42 & b_42; // @[CSA.scala 45:21]
  wire  sum_42 = a_xor_b42 ^ cin_42; // @[CSA.scala 46:23]
  wire  cout_42 = a_and_b42 | a_xor_b42 & cin_42; // @[CSA.scala 47:24]
  wire [1:0] temp_42 = {cout_42,sum_42}; // @[Cat.scala 31:58]
  wire  a_43 = io_in_0[43]; // @[CSA.scala 43:32]
  wire  b_43 = io_in_1[43]; // @[CSA.scala 43:45]
  wire  cin_43 = io_in_2[43]; // @[CSA.scala 43:58]
  wire  a_xor_b43 = a_43 ^ b_43; // @[CSA.scala 44:21]
  wire  a_and_b43 = a_43 & b_43; // @[CSA.scala 45:21]
  wire  sum_43 = a_xor_b43 ^ cin_43; // @[CSA.scala 46:23]
  wire  cout_43 = a_and_b43 | a_xor_b43 & cin_43; // @[CSA.scala 47:24]
  wire [1:0] temp_43 = {cout_43,sum_43}; // @[Cat.scala 31:58]
  wire  a_44 = io_in_0[44]; // @[CSA.scala 43:32]
  wire  b_44 = io_in_1[44]; // @[CSA.scala 43:45]
  wire  cin_44 = io_in_2[44]; // @[CSA.scala 43:58]
  wire  a_xor_b44 = a_44 ^ b_44; // @[CSA.scala 44:21]
  wire  a_and_b44 = a_44 & b_44; // @[CSA.scala 45:21]
  wire  sum_44 = a_xor_b44 ^ cin_44; // @[CSA.scala 46:23]
  wire  cout_44 = a_and_b44 | a_xor_b44 & cin_44; // @[CSA.scala 47:24]
  wire [1:0] temp_44 = {cout_44,sum_44}; // @[Cat.scala 31:58]
  wire  a_45 = io_in_0[45]; // @[CSA.scala 43:32]
  wire  b_45 = io_in_1[45]; // @[CSA.scala 43:45]
  wire  cin_45 = io_in_2[45]; // @[CSA.scala 43:58]
  wire  a_xor_b45 = a_45 ^ b_45; // @[CSA.scala 44:21]
  wire  a_and_b45 = a_45 & b_45; // @[CSA.scala 45:21]
  wire  sum_45 = a_xor_b45 ^ cin_45; // @[CSA.scala 46:23]
  wire  cout_45 = a_and_b45 | a_xor_b45 & cin_45; // @[CSA.scala 47:24]
  wire [1:0] temp_45 = {cout_45,sum_45}; // @[Cat.scala 31:58]
  wire  a_46 = io_in_0[46]; // @[CSA.scala 43:32]
  wire  b_46 = io_in_1[46]; // @[CSA.scala 43:45]
  wire  cin_46 = io_in_2[46]; // @[CSA.scala 43:58]
  wire  a_xor_b46 = a_46 ^ b_46; // @[CSA.scala 44:21]
  wire  a_and_b46 = a_46 & b_46; // @[CSA.scala 45:21]
  wire  sum_46 = a_xor_b46 ^ cin_46; // @[CSA.scala 46:23]
  wire  cout_46 = a_and_b46 | a_xor_b46 & cin_46; // @[CSA.scala 47:24]
  wire [1:0] temp_46 = {cout_46,sum_46}; // @[Cat.scala 31:58]
  wire  a_47 = io_in_0[47]; // @[CSA.scala 43:32]
  wire  b_47 = io_in_1[47]; // @[CSA.scala 43:45]
  wire  cin_47 = io_in_2[47]; // @[CSA.scala 43:58]
  wire  a_xor_b47 = a_47 ^ b_47; // @[CSA.scala 44:21]
  wire  a_and_b47 = a_47 & b_47; // @[CSA.scala 45:21]
  wire  sum_47 = a_xor_b47 ^ cin_47; // @[CSA.scala 46:23]
  wire  cout_47 = a_and_b47 | a_xor_b47 & cin_47; // @[CSA.scala 47:24]
  wire [1:0] temp_47 = {cout_47,sum_47}; // @[Cat.scala 31:58]
  wire  a_48 = io_in_0[48]; // @[CSA.scala 43:32]
  wire  b_48 = io_in_1[48]; // @[CSA.scala 43:45]
  wire  cin_48 = io_in_2[48]; // @[CSA.scala 43:58]
  wire  a_xor_b48 = a_48 ^ b_48; // @[CSA.scala 44:21]
  wire  a_and_b48 = a_48 & b_48; // @[CSA.scala 45:21]
  wire  sum_48 = a_xor_b48 ^ cin_48; // @[CSA.scala 46:23]
  wire  cout_48 = a_and_b48 | a_xor_b48 & cin_48; // @[CSA.scala 47:24]
  wire [1:0] temp_48 = {cout_48,sum_48}; // @[Cat.scala 31:58]
  wire  a_49 = io_in_0[49]; // @[CSA.scala 43:32]
  wire  b_49 = io_in_1[49]; // @[CSA.scala 43:45]
  wire  cin_49 = io_in_2[49]; // @[CSA.scala 43:58]
  wire  a_xor_b49 = a_49 ^ b_49; // @[CSA.scala 44:21]
  wire  a_and_b49 = a_49 & b_49; // @[CSA.scala 45:21]
  wire  sum_49 = a_xor_b49 ^ cin_49; // @[CSA.scala 46:23]
  wire  cout_49 = a_and_b49 | a_xor_b49 & cin_49; // @[CSA.scala 47:24]
  wire [1:0] temp_49 = {cout_49,sum_49}; // @[Cat.scala 31:58]
  wire  a_50 = io_in_0[50]; // @[CSA.scala 43:32]
  wire  b_50 = io_in_1[50]; // @[CSA.scala 43:45]
  wire  cin_50 = io_in_2[50]; // @[CSA.scala 43:58]
  wire  a_xor_b50 = a_50 ^ b_50; // @[CSA.scala 44:21]
  wire  a_and_b50 = a_50 & b_50; // @[CSA.scala 45:21]
  wire  sum_50 = a_xor_b50 ^ cin_50; // @[CSA.scala 46:23]
  wire  cout_50 = a_and_b50 | a_xor_b50 & cin_50; // @[CSA.scala 47:24]
  wire [1:0] temp_50 = {cout_50,sum_50}; // @[Cat.scala 31:58]
  wire  a_51 = io_in_0[51]; // @[CSA.scala 43:32]
  wire  b_51 = io_in_1[51]; // @[CSA.scala 43:45]
  wire  cin_51 = io_in_2[51]; // @[CSA.scala 43:58]
  wire  a_xor_b51 = a_51 ^ b_51; // @[CSA.scala 44:21]
  wire  a_and_b51 = a_51 & b_51; // @[CSA.scala 45:21]
  wire  sum_51 = a_xor_b51 ^ cin_51; // @[CSA.scala 46:23]
  wire  cout_51 = a_and_b51 | a_xor_b51 & cin_51; // @[CSA.scala 47:24]
  wire [1:0] temp_51 = {cout_51,sum_51}; // @[Cat.scala 31:58]
  wire  a_52 = io_in_0[52]; // @[CSA.scala 43:32]
  wire  b_52 = io_in_1[52]; // @[CSA.scala 43:45]
  wire  cin_52 = io_in_2[52]; // @[CSA.scala 43:58]
  wire  a_xor_b52 = a_52 ^ b_52; // @[CSA.scala 44:21]
  wire  a_and_b52 = a_52 & b_52; // @[CSA.scala 45:21]
  wire  sum_52 = a_xor_b52 ^ cin_52; // @[CSA.scala 46:23]
  wire  cout_52 = a_and_b52 | a_xor_b52 & cin_52; // @[CSA.scala 47:24]
  wire [1:0] temp_52 = {cout_52,sum_52}; // @[Cat.scala 31:58]
  wire  a_53 = io_in_0[53]; // @[CSA.scala 43:32]
  wire  b_53 = io_in_1[53]; // @[CSA.scala 43:45]
  wire  cin_53 = io_in_2[53]; // @[CSA.scala 43:58]
  wire  a_xor_b53 = a_53 ^ b_53; // @[CSA.scala 44:21]
  wire  a_and_b53 = a_53 & b_53; // @[CSA.scala 45:21]
  wire  sum_53 = a_xor_b53 ^ cin_53; // @[CSA.scala 46:23]
  wire  cout_53 = a_and_b53 | a_xor_b53 & cin_53; // @[CSA.scala 47:24]
  wire [1:0] temp_53 = {cout_53,sum_53}; // @[Cat.scala 31:58]
  wire  a_54 = io_in_0[54]; // @[CSA.scala 43:32]
  wire  b_54 = io_in_1[54]; // @[CSA.scala 43:45]
  wire  cin_54 = io_in_2[54]; // @[CSA.scala 43:58]
  wire  a_xor_b54 = a_54 ^ b_54; // @[CSA.scala 44:21]
  wire  a_and_b54 = a_54 & b_54; // @[CSA.scala 45:21]
  wire  sum_54 = a_xor_b54 ^ cin_54; // @[CSA.scala 46:23]
  wire  cout_54 = a_and_b54 | a_xor_b54 & cin_54; // @[CSA.scala 47:24]
  wire [1:0] temp_54 = {cout_54,sum_54}; // @[Cat.scala 31:58]
  wire  a_55 = io_in_0[55]; // @[CSA.scala 43:32]
  wire  b_55 = io_in_1[55]; // @[CSA.scala 43:45]
  wire  cin_55 = io_in_2[55]; // @[CSA.scala 43:58]
  wire  a_xor_b55 = a_55 ^ b_55; // @[CSA.scala 44:21]
  wire  a_and_b55 = a_55 & b_55; // @[CSA.scala 45:21]
  wire  sum_55 = a_xor_b55 ^ cin_55; // @[CSA.scala 46:23]
  wire  cout_55 = a_and_b55 | a_xor_b55 & cin_55; // @[CSA.scala 47:24]
  wire [1:0] temp_55 = {cout_55,sum_55}; // @[Cat.scala 31:58]
  wire  a_56 = io_in_0[56]; // @[CSA.scala 43:32]
  wire  b_56 = io_in_1[56]; // @[CSA.scala 43:45]
  wire  cin_56 = io_in_2[56]; // @[CSA.scala 43:58]
  wire  a_xor_b56 = a_56 ^ b_56; // @[CSA.scala 44:21]
  wire  a_and_b56 = a_56 & b_56; // @[CSA.scala 45:21]
  wire  sum_56 = a_xor_b56 ^ cin_56; // @[CSA.scala 46:23]
  wire  cout_56 = a_and_b56 | a_xor_b56 & cin_56; // @[CSA.scala 47:24]
  wire [1:0] temp_56 = {cout_56,sum_56}; // @[Cat.scala 31:58]
  wire  a_57 = io_in_0[57]; // @[CSA.scala 43:32]
  wire  b_57 = io_in_1[57]; // @[CSA.scala 43:45]
  wire  cin_57 = io_in_2[57]; // @[CSA.scala 43:58]
  wire  a_xor_b57 = a_57 ^ b_57; // @[CSA.scala 44:21]
  wire  a_and_b57 = a_57 & b_57; // @[CSA.scala 45:21]
  wire  sum_57 = a_xor_b57 ^ cin_57; // @[CSA.scala 46:23]
  wire  cout_57 = a_and_b57 | a_xor_b57 & cin_57; // @[CSA.scala 47:24]
  wire [1:0] temp_57 = {cout_57,sum_57}; // @[Cat.scala 31:58]
  wire  a_58 = io_in_0[58]; // @[CSA.scala 43:32]
  wire  b_58 = io_in_1[58]; // @[CSA.scala 43:45]
  wire  cin_58 = io_in_2[58]; // @[CSA.scala 43:58]
  wire  a_xor_b58 = a_58 ^ b_58; // @[CSA.scala 44:21]
  wire  a_and_b58 = a_58 & b_58; // @[CSA.scala 45:21]
  wire  sum_58 = a_xor_b58 ^ cin_58; // @[CSA.scala 46:23]
  wire  cout_58 = a_and_b58 | a_xor_b58 & cin_58; // @[CSA.scala 47:24]
  wire [1:0] temp_58 = {cout_58,sum_58}; // @[Cat.scala 31:58]
  wire  a_59 = io_in_0[59]; // @[CSA.scala 43:32]
  wire  b_59 = io_in_1[59]; // @[CSA.scala 43:45]
  wire  cin_59 = io_in_2[59]; // @[CSA.scala 43:58]
  wire  a_xor_b59 = a_59 ^ b_59; // @[CSA.scala 44:21]
  wire  a_and_b59 = a_59 & b_59; // @[CSA.scala 45:21]
  wire  sum_59 = a_xor_b59 ^ cin_59; // @[CSA.scala 46:23]
  wire  cout_59 = a_and_b59 | a_xor_b59 & cin_59; // @[CSA.scala 47:24]
  wire [1:0] temp_59 = {cout_59,sum_59}; // @[Cat.scala 31:58]
  wire  a_60 = io_in_0[60]; // @[CSA.scala 43:32]
  wire  b_60 = io_in_1[60]; // @[CSA.scala 43:45]
  wire  cin_60 = io_in_2[60]; // @[CSA.scala 43:58]
  wire  a_xor_b60 = a_60 ^ b_60; // @[CSA.scala 44:21]
  wire  a_and_b60 = a_60 & b_60; // @[CSA.scala 45:21]
  wire  sum_60 = a_xor_b60 ^ cin_60; // @[CSA.scala 46:23]
  wire  cout_60 = a_and_b60 | a_xor_b60 & cin_60; // @[CSA.scala 47:24]
  wire [1:0] temp_60 = {cout_60,sum_60}; // @[Cat.scala 31:58]
  wire  a_61 = io_in_0[61]; // @[CSA.scala 43:32]
  wire  b_61 = io_in_1[61]; // @[CSA.scala 43:45]
  wire  cin_61 = io_in_2[61]; // @[CSA.scala 43:58]
  wire  a_xor_b61 = a_61 ^ b_61; // @[CSA.scala 44:21]
  wire  a_and_b61 = a_61 & b_61; // @[CSA.scala 45:21]
  wire  sum_61 = a_xor_b61 ^ cin_61; // @[CSA.scala 46:23]
  wire  cout_61 = a_and_b61 | a_xor_b61 & cin_61; // @[CSA.scala 47:24]
  wire [1:0] temp_61 = {cout_61,sum_61}; // @[Cat.scala 31:58]
  wire  a_62 = io_in_0[62]; // @[CSA.scala 43:32]
  wire  b_62 = io_in_1[62]; // @[CSA.scala 43:45]
  wire  cin_62 = io_in_2[62]; // @[CSA.scala 43:58]
  wire  a_xor_b62 = a_62 ^ b_62; // @[CSA.scala 44:21]
  wire  a_and_b62 = a_62 & b_62; // @[CSA.scala 45:21]
  wire  sum_62 = a_xor_b62 ^ cin_62; // @[CSA.scala 46:23]
  wire  cout_62 = a_and_b62 | a_xor_b62 & cin_62; // @[CSA.scala 47:24]
  wire [1:0] temp_62 = {cout_62,sum_62}; // @[Cat.scala 31:58]
  wire  a_63 = io_in_0[63]; // @[CSA.scala 43:32]
  wire  b_63 = io_in_1[63]; // @[CSA.scala 43:45]
  wire  cin_63 = io_in_2[63]; // @[CSA.scala 43:58]
  wire  a_xor_b63 = a_63 ^ b_63; // @[CSA.scala 44:21]
  wire  a_and_b63 = a_63 & b_63; // @[CSA.scala 45:21]
  wire  sum_63 = a_xor_b63 ^ cin_63; // @[CSA.scala 46:23]
  wire  cout_63 = a_and_b63 | a_xor_b63 & cin_63; // @[CSA.scala 47:24]
  wire [1:0] temp_63 = {cout_63,sum_63}; // @[Cat.scala 31:58]
  wire  a_64 = io_in_0[64]; // @[CSA.scala 43:32]
  wire  b_64 = io_in_1[64]; // @[CSA.scala 43:45]
  wire  cin_64 = io_in_2[64]; // @[CSA.scala 43:58]
  wire  a_xor_b64 = a_64 ^ b_64; // @[CSA.scala 44:21]
  wire  a_and_b64 = a_64 & b_64; // @[CSA.scala 45:21]
  wire  sum_64 = a_xor_b64 ^ cin_64; // @[CSA.scala 46:23]
  wire  cout_64 = a_and_b64 | a_xor_b64 & cin_64; // @[CSA.scala 47:24]
  wire [1:0] temp_64 = {cout_64,sum_64}; // @[Cat.scala 31:58]
  wire  a_65 = io_in_0[65]; // @[CSA.scala 43:32]
  wire  b_65 = io_in_1[65]; // @[CSA.scala 43:45]
  wire  cin_65 = io_in_2[65]; // @[CSA.scala 43:58]
  wire  a_xor_b65 = a_65 ^ b_65; // @[CSA.scala 44:21]
  wire  a_and_b65 = a_65 & b_65; // @[CSA.scala 45:21]
  wire  sum_65 = a_xor_b65 ^ cin_65; // @[CSA.scala 46:23]
  wire  cout_65 = a_and_b65 | a_xor_b65 & cin_65; // @[CSA.scala 47:24]
  wire [1:0] temp_65 = {cout_65,sum_65}; // @[Cat.scala 31:58]
  wire  a_66 = io_in_0[66]; // @[CSA.scala 43:32]
  wire  b_66 = io_in_1[66]; // @[CSA.scala 43:45]
  wire  cin_66 = io_in_2[66]; // @[CSA.scala 43:58]
  wire  a_xor_b66 = a_66 ^ b_66; // @[CSA.scala 44:21]
  wire  a_and_b66 = a_66 & b_66; // @[CSA.scala 45:21]
  wire  sum_66 = a_xor_b66 ^ cin_66; // @[CSA.scala 46:23]
  wire  cout_66 = a_and_b66 | a_xor_b66 & cin_66; // @[CSA.scala 47:24]
  wire [1:0] temp_66 = {cout_66,sum_66}; // @[Cat.scala 31:58]
  wire  a_67 = io_in_0[67]; // @[CSA.scala 43:32]
  wire  b_67 = io_in_1[67]; // @[CSA.scala 43:45]
  wire  cin_67 = io_in_2[67]; // @[CSA.scala 43:58]
  wire  a_xor_b67 = a_67 ^ b_67; // @[CSA.scala 44:21]
  wire  a_and_b67 = a_67 & b_67; // @[CSA.scala 45:21]
  wire  sum_67 = a_xor_b67 ^ cin_67; // @[CSA.scala 46:23]
  wire  cout_67 = a_and_b67 | a_xor_b67 & cin_67; // @[CSA.scala 47:24]
  wire [1:0] temp_67 = {cout_67,sum_67}; // @[Cat.scala 31:58]
  wire [7:0] io_out_0_lo_lo_lo = {temp_7[0],temp_6[0],temp_5[0],temp_4[0],temp_3[0],temp_2[0],temp_1[0],temp_0[0]}; // @[Cat.scala 31:58]
  wire [16:0] io_out_0_lo_lo = {temp_16[0],temp_15[0],temp_14[0],temp_13[0],temp_12[0],temp_11[0],temp_10[0],temp_9[0],
    temp_8[0],io_out_0_lo_lo_lo}; // @[Cat.scala 31:58]
  wire [7:0] io_out_0_lo_hi_lo = {temp_24[0],temp_23[0],temp_22[0],temp_21[0],temp_20[0],temp_19[0],temp_18[0],temp_17[0
    ]}; // @[Cat.scala 31:58]
  wire [16:0] io_out_0_lo_hi = {temp_33[0],temp_32[0],temp_31[0],temp_30[0],temp_29[0],temp_28[0],temp_27[0],temp_26[0],
    temp_25[0],io_out_0_lo_hi_lo}; // @[Cat.scala 31:58]
  wire [33:0] io_out_0_lo = {io_out_0_lo_hi,io_out_0_lo_lo}; // @[Cat.scala 31:58]
  wire [7:0] io_out_0_hi_lo_lo = {temp_41[0],temp_40[0],temp_39[0],temp_38[0],temp_37[0],temp_36[0],temp_35[0],temp_34[0
    ]}; // @[Cat.scala 31:58]
  wire [16:0] io_out_0_hi_lo = {temp_50[0],temp_49[0],temp_48[0],temp_47[0],temp_46[0],temp_45[0],temp_44[0],temp_43[0],
    temp_42[0],io_out_0_hi_lo_lo}; // @[Cat.scala 31:58]
  wire [7:0] io_out_0_hi_hi_lo = {temp_58[0],temp_57[0],temp_56[0],temp_55[0],temp_54[0],temp_53[0],temp_52[0],temp_51[0
    ]}; // @[Cat.scala 31:58]
  wire [16:0] io_out_0_hi_hi = {temp_67[0],temp_66[0],temp_65[0],temp_64[0],temp_63[0],temp_62[0],temp_61[0],temp_60[0],
    temp_59[0],io_out_0_hi_hi_lo}; // @[Cat.scala 31:58]
  wire [33:0] io_out_0_hi = {io_out_0_hi_hi,io_out_0_hi_lo}; // @[Cat.scala 31:58]
  wire [7:0] io_out_1_lo_lo_lo = {temp_7[1],temp_6[1],temp_5[1],temp_4[1],temp_3[1],temp_2[1],temp_1[1],temp_0[1]}; // @[Cat.scala 31:58]
  wire [16:0] io_out_1_lo_lo = {temp_16[1],temp_15[1],temp_14[1],temp_13[1],temp_12[1],temp_11[1],temp_10[1],temp_9[1],
    temp_8[1],io_out_1_lo_lo_lo}; // @[Cat.scala 31:58]
  wire [7:0] io_out_1_lo_hi_lo = {temp_24[1],temp_23[1],temp_22[1],temp_21[1],temp_20[1],temp_19[1],temp_18[1],temp_17[1
    ]}; // @[Cat.scala 31:58]
  wire [16:0] io_out_1_lo_hi = {temp_33[1],temp_32[1],temp_31[1],temp_30[1],temp_29[1],temp_28[1],temp_27[1],temp_26[1],
    temp_25[1],io_out_1_lo_hi_lo}; // @[Cat.scala 31:58]
  wire [33:0] io_out_1_lo = {io_out_1_lo_hi,io_out_1_lo_lo}; // @[Cat.scala 31:58]
  wire [7:0] io_out_1_hi_lo_lo = {temp_41[1],temp_40[1],temp_39[1],temp_38[1],temp_37[1],temp_36[1],temp_35[1],temp_34[1
    ]}; // @[Cat.scala 31:58]
  wire [16:0] io_out_1_hi_lo = {temp_50[1],temp_49[1],temp_48[1],temp_47[1],temp_46[1],temp_45[1],temp_44[1],temp_43[1],
    temp_42[1],io_out_1_hi_lo_lo}; // @[Cat.scala 31:58]
  wire [7:0] io_out_1_hi_hi_lo = {temp_58[1],temp_57[1],temp_56[1],temp_55[1],temp_54[1],temp_53[1],temp_52[1],temp_51[1
    ]}; // @[Cat.scala 31:58]
  wire [16:0] io_out_1_hi_hi = {temp_67[1],temp_66[1],temp_65[1],temp_64[1],temp_63[1],temp_62[1],temp_61[1],temp_60[1],
    temp_59[1],io_out_1_hi_hi_lo}; // @[Cat.scala 31:58]
  wire [33:0] io_out_1_hi = {io_out_1_hi_hi,io_out_1_hi_lo}; // @[Cat.scala 31:58]
  assign io_out_0 = {io_out_0_hi,io_out_0_lo}; // @[Cat.scala 31:58]
  assign io_out_1 = {io_out_1_hi,io_out_1_lo}; // @[Cat.scala 31:58]
endmodule
