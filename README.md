Here is a java Swing application for exploring the symmetrical aperiodic tilings based on the de Brujin's multigrid algorithm.
I was specifically interested in playing with the Penrose rhombi (Symmetry equals 5), so this application generates some variations specifically for this tiling.

It was an interesting task to find out how to calculate the direction of the arrows that specify the matching rules.
The direction of a rhombus is not automatically obvious when you use the multigrid algorithm. 

Note that the Penrose arrows, Kites and Darts and all other variations work well only for the symmetrical tilings, when the offset gives an integer when multiplied by 5.
The offset values like 0.2, 0.4, 0.6, 0.8 give the correct and symmetrical Penrose tilings. For values 0.4 and 0.6 click the "Reverse rhombi" combobox to fix the directions. 

If you know how to tweak the multigrid algorithm to make it generate the 7-fold tilings which respect the matching rules similar to the Penrose arrows, please drop me a note in the Discussion tab.  

"Color by indices" gives you a glimpse of a higher-dimensional structure behind the multigrid.

<p align="center">
  <img src="https://github.com/user-attachments/assets/df5d7cff-3a0c-4321-9fff-f583d7b339c4" width="30%" />
  <img src="https://github.com/user-attachments/assets/5bcbcf5c-f711-4a07-9ba6-262023bc08b3" width="30%" />
  <img src="https://github.com/user-attachments/assets/1e273ff6-4c83-4948-b1ac-194d01b8e80a" width="30%" />  

</p>

Have fun!  
alexp
