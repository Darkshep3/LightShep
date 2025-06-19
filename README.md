# LightShep
fun AP CSA project for a chess engine that i'm improving 
6/18/2025 
  A few weeks after my AP CSA Class Final Project Presentation and also since junior year is over, this is a brief summary what I have: 
  Search: Alpha-beta pruning + negamaxing + quiescence, minor move ordering based on captures/enpassant
  Evaluation: piece square tables (from rofchade) for middlegame/endgame with interpolating, minor bonuses for pawn structures
  Legal Move Generator: Pseudo-legal then remove if not possible, bitboard architecture but no magic bitboards yet, makeMove / unMakeMove 
  UCI Compatibility (thx to gemini) 

  Limitations: Depth 5 is the limit before moves take time
