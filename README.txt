Virtual Scavenger
By: Manuel Perez, Alex King, Ryan Born
 
 
 This application is the base for a simple scavenger hunt game made
 exclusively for display to a college class. The application itself
 uses augmented reality technology to display puzzle boxes in 3D
 when specific targets are identified.
 
 In-game Specifications as stated on the Start screen
 Each Level has the following targets:
 -One puzzle which will unlock the next level
 -One health pack to replenish health
 -Three "poisons" which deplete health
 
 Instructions:
 -Point Camera at target to view
 -One health pack to replenish health
 -DOuble tap to access the menu
 
 It is important to note this application was built upon the Vuforia
 augmented reality technology sample code provided by Qualcom. The
 application uses target recognition to recognize any of the five
 pre-loaded textures maps that serve as targets. These targets have
 distinct vertices and edges that allow for easy recognition. Once the
 application recognizes the target using the mobile device's camera,
 a 3-D box is displayed on top of the identified target when observed
 through the device's screen.
 
 In order to display the box a 3-D cube is first rendered using
 OpenGL ES 2.0. There are different textures available to represent
 different aspects of the game. While the box is not selected the
 texture is set to Mario Bro's question block. The Vuforia technology
 allows us to display the box on top of the detected target when
 observed throughout the device's screen. This is done by mixing live
 video with the 3-D image making the virtual image appear like part of
 the real environment.
 
 A button is also created on the same surface as the box. This way the
 user can virtually touch the box to trigger an event. When an event is
 triggered the texture of the box will change to display a randomly
 preselected event. The textures and events belonging to each specific
 do not change until the user advances to another level. One of the
 trigger events is a puzzle that opens a new screen with a multiple
 answer choice riddle.
 
 Once the riddle is answered, whether the answer is correct or not, new
 textures are reassigned to each target at random and all revealed
 events default back to a Mario Bros' question box texture. A custom
 Menu was also made to change some of the game's settings or access
 health packs.

 Notes: 
 
-The original test code is part of Qualcom's Vuforia technology examples.
-The application uses the victoria's Secret logo as well as Mario Bros
block textures. We do not claim ownership over this logo and textures.
-In order to code using Vuforia technology a license must be issued from
Qualcom.
