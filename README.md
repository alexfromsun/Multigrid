Here is a java Swing application for exploring the symmetrical aperiodic tiling patterns based on de Brujin's multigrid algorithm.
I was specifically interested in playing with the Penrose rhombi tiling (Symmetry equals 5), so this application generates some variations specifically for this tiling.

It was an interesting task to find out how to calculate the direction of rhombi side arrows which specify the matching rule.
The direction of a rhombus is not automatically obvious when you use the multigrid algorithm. 

Note that the Penrose arrows, Kite and Darts and all the rest options work well only for the symmetrical tilings, when the Offset gives an integer when multiplied by 5.
The offset values like 0.2, 0.4, 0.6, 0.8 give the correct and symmetrical Penrose tilings. For values 0.4 and 0.6 click the "Reverse rhombi" combobox to fix the arrows. 

"Color by indices" provides a glimpse to a higher dimensions structure behind the multigrid. 

Have fun!
alexp
