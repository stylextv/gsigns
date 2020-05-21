# GSigns

GSigns is a spigot plugin that allows the creation of item frames that hold images and gifs.
The spigotmc page, including a download link, can be found [here](https://www.spigotmc.org/resources/g-signs-a-unique-map-signs-plugin-for-lobbies.73693/).

## Map Sending

Instead of sending a giant packet that holds the entire map data each time the sign is updated, the map data is send once to player as he walks up to the sign. After that premade entity metadata packets for the item frames are send that tell the client which of the maps it received earlier needs to be displayed.
The sending of the maps is also optimized as the images are not being rendered onto the map via MapCanvas#drawImage which would use the very slow MapPalette#matchColor function. Rather the r, g and b values will be pre converted into the corresponding byte colors which will then be put onto the map:
```java
for(int i=0; i<data.length; i++) {
	canvas.setPixel(i%128, i/128, data[i]);
}
```

## GSIGN-Format

When saving a single item frame to a file, for example when the server gets restarted, a special format is used.
First a file is created with a name that holds information about the location of the item frame. The name is structured in the following way:
```
WORLD_NAME,X,Y,Z,FACING,DELAY
```
"FACING" is the direction the item frame is facing.
"DELAY" is the amount of milliseconds between frames. If the frame is a still image the value is 0.

After that the file will now be filled with the data of the map views. Each map view (or frame) will be stored in order, one after the other:
1. The map id of the map is stored as `4` bytes.
1. The pixel data will be stored as a byte array of size `16384`, as each frame has a width and height of 128.

When each mapview has been stored the entire map data will be compressed using the Deflater to reduce file size:
```java
Deflater compressor = new Deflater();
compressor.setLevel(Deflater.BEST_SPEED);
```

GSIGN-files in the "signs" folder that do not follow this format will be deleted upon loading for being outdated or corrupted.

## Sign Coding
Each code is structured like this: {ARGUMENT1:VALUE1,ARGUMENT2:VALUE2,ARGUMENT3:VALUE3, ...}
It is important that their is no space between two arguments.
If you want to use spaces or the character ' , ' inside values put the value in quotes like this: txt:"Text"
If you want to use a ' " ' inside the quote use: \"
For ' \ ' use: \\

Here is a list of arguments you can use:

txt  | The text that is rendered.

txt-col  | The text color in hexadecimal. Default: RGB(255,255,255) (White)

bg-url  | The url to the background. Default: none (results in black background).
If gifs don't work copy the gif into "GamemodeSigns/images" and use bg-img.

bg-img  | The image for the background (in "plugins\GamemodeSigns\images").
Default: none (results in black background)

bg-blur  | The radius of the blur that is applied onto the background [Integer]. Default: 0 -> no blur

bg-bright  | Allows you to control the brightness of the background [Float]. Default: 1

dith  | Should the image be dithered [Boolean: true/false]. Default: true

fnt  | The font family and style [NAME-STYLE].
It is important that the name and the style have no spaces in it. Default: "Raleway-Bold"
Examples:
Roboto-Regular; Roboto-Black (https://fonts.google.com/specimen/Roboto)
UbuntuMono-BoldItalic (https://fonts.google.com/specimen/Ubuntu+Mono)
Pacifico-Regular (https://fonts.google.com/specimen/Pacifico)
More fonts can be found here: https://fonts.google.com/.

fnt-siz  | The font size that is used for text rendering [Integer]. Default: 72

fnt-sty  | The font style that is used for text rendering [Integer; PLAIN, BOLD, ITALIC (0-2)]. Default: 0

sim-hue  | The hue of the color of the simplex noise [Float, 0-1].
Default: none -> no simplex noise. "sim-hue:rdm" results in a random hue.

sim-seed  | The seed used for the simplex noise texture [Integer]. Default: Random number from 1 to 1000.

sim-siz  | The size of the noise intervals [Double]. Default: 150.

Here are some example codes:
- {txt:Hello,fnt:"Pacifico-Regular"}
- {fnt-siz:120,txt:TEXT,bg-url:https://raw.githubusercontent.com/StylexTV/GSigns/master/showcase/bg0.png}
- {txt:Bedwars,sim-hue:0.94}
