Here is a java Swing application for exploring the symmetrical aperiodic tiling patterns based on de Brujin's multigrid algorithm.
I was specifically interested in playing with the Penrose rhombi tiling (Symmetry equals 5), so this application generates some variations of this tiling.

It was an interesting task to find out how to calculate the direction of rhombi side arrows which specify the matching rule.
The direction of a rhombus is not automatically obvious when you use the multigrid algorithm. 
I even had to spend 50 euros to by an e-book "A Guide to Penrose Tilings" to understand how I can infer the direction of a rhombus based on the indexes of its verticies.

Note that the Penrose arrows, Kite and Darts and all the rest variations work well only for the symmetrica tilings, when the Offset gives an integer when multiplied by 5.
The offset values like 0.2, 0.4, 0.6, 0.8 give the correct and symmetrical Penrose tilings. For values 0.4 and 0.6 click the "Reverse rhombi" combobox to fix the arrows. 

"Color by indices" provides a hint to the connection of a tiling with the 
