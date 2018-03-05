# The-Pizza-Problem
A solution to the pizza problem given as a practice problem for Hash code 2018

The description of the question is attached above and the code is documented through out.
The problem is solved as follows:
1. The algorithm reads the input from the file and saves the coordinates of the element (either mushrooms or tomatoes) with the smallest presence.
2. The algorithm picks as much slices as possible, based on several conditions.
  a.Start from left to right, pick the first coordinate of the smallest element
  b.Create a slice which has as small amount of 'smallest element' as possible
  c.Check if one of the possibility is a corner possibility, if there is remove the others
  c.Return the one with the largest size.
3. Repeat step 2 for all the smallest elements not included in a slice that have a possibility to be included
4. Try to expand to elements not covered with slices that can expand
