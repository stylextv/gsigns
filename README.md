<h1 align="center">
  <br>
  <img src="https://raw.githubusercontent.com/StylexTV/GSigns/master/showcase/socials/cover.png">
  <br>
</h1>

<h4 align="center">🚩 Source code of the GSigns spigot plugin, made with love in Java.</h4>

<p align="center">
  <a href="https://GitHub.com/StylexTV/GSigns/stargazers/">
    <img alt="stars" src="https://img.shields.io/github/stars/StylexTV/GSigns.svg?color=ffdd00"/>
  </a>
  [![Codacy Badge](https://app.codacy.com/project/badge/Grade/a33dbb19ff17460d896a7864fececab6)](https://www.codacy.com/manual/noluck942/GSigns?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=StylexTV/GSigns&amp;utm_campaign=Badge_Grade)
  ![Code size](https://img.shields.io/github/languages/code-size/StylexTV/GSigns.svg)
  ![GitHub repo size](https://img.shields.io/github/repo-size/StylexTV/GSigns.svg)
  ![Lines of Code](https://tokei.rs/b1/github/StylexTV/GSigns?category=code)
</p>

GSigns is a spigot plugin that allows the creation of item frames that hold images and gifs.
> The spigotmc page, including a download link, can be found [here](https://www.spigotmc.org/resources/g-signs-a-unique-map-signs-plugin-for-lobbies.85017/).

## Map Sending

Instead of sending a **huge packet** containing all the map data **every time** the sign is updated, the map data is sent once to the player as he approaches the sign. Custom entity metadata packets are then sent for the item frames, telling the client which of the previously received maps must be displayed. Sending the maps is also optimized because the images are not rendered to the map via `MapCanvas#drawImage`, which would use the very slow `MapPalette#matchColor` function. Instead, the r, g and b values are pre-converted to the corresponding byte colors, which are then transferred to the map packet:
```java
PacketPlayOutMap packet = new PacketPlayOutMap(mapId, (byte) 0, false, false, new ArrayList<>(), bytes, 0, 0, 128, 128);
```

## GSIGN-Format

When saving a whole sign to a single file, for example when the server gets restarted, a special format is used.
First a file with the smallest unused number (starting with 0) as a name is created. Then a header consisting of `45` bytes is placed at the beginning of the file:
```bash
# 📎 SIGN_UUID (16 bytes)
The UUID of the sign. Every sign has a different UUID.
This is used e.g. when teleporting to a sign with a given UUID as command argument.

# 🌎 WORLD_UUID (16 bytes)
The UUID of the world.

# 🧭 FACING (1 byte)
The direction the item frames are facing.

# 📄 AMOUNT OF ITEM FRAMES (4 bytes)
The number of item frames that make up this sign.

# 📏 WIDTH & HEIGHT (2 * 4 bytes)
The width and height of the sign in item frames.
```

Afterwards the file is filled with the item frames of the sign and their images. Every item frame has a `16` bytes long header that is put at the beginning of the chunk in the file that is reserved for this particular item frame:
```bash
# 📍 X,Y,Z (3 * 4 bytes)
The x-, y-, and z-coordinate.

# 📄 AMOUNT OF IMAGES (4 bytes)
The number of images that make up this item frame.
```

The actual data of the images are placed at the end of the chunk. Each image (or each frame in a gif) is saved in sequence:
1. the delay is stored as `4` bytes. This is the number of milliseconds between frames. If the map is a still image, the value is 0.
1. the pixel data is stored as a byte array of length `16384`, since each frame has a width and height of 128.

When each item frame and its data has been stored the entire file data will be compressed using the Deflater to reduce file size:
```java
Deflater compressor = new Deflater();
compressor.setLevel(Deflater.BEST_SPEED);
```

.GSIGN files in the "signs" folder that do not conform to this format are deleted on loading because they are outdated or damaged.


## Sign Coding

Each code is structured like this:
```bash
{ARGUMENT1:VALUE1,ARGUMENT2:VALUE2,ARGUMENT3:VALUE3, ...}
```
It is important, that there is no space between two arguments.
If you want to use spaces or the character `,` inside values put the value in quotes like this: `txt:"Text"`
If you want to use a `"` inside the quote use: `\"`
For `\` use: `\\`

Here is a list of arguments you can use:
Argument Name | Description
------------ | -------------
txt | The text that is rendered.
txt-col | The text color in hexadecimal. Default: RGB(255,255,255) (White)
bg-col | The color of the background in hexadecimal. Default: none (results in black background)
bg-url | The url to the background. Default: none (results in black background). If gifs don't work copy the gif into "GamemodeSigns/images" and use bg-img.
bg-img | The image for the background (in "plugins\GamemodeSigns\images"). Default: none (results in black background)
bg-blur | The radius of the blur that is applied onto the background [Integer]. Default: 0 -> no blur
bg-bright | Allows you to control the brightness of the background [Float]. Default: 1
dith | Should the image be dithered [Boolean: true/false]. Default: true
fnt | The font family and style [NAME-STYLE]. It is important that the name and the style have no spaces in it. Default: "Raleway-Bold". Examples: Roboto-Regular; Roboto-Black (https://fonts.google.com/specimen/Roboto), UbuntuMono-BoldItalic (https://fonts.google.com/specimen/Ubuntu+Mono), Pacifico-Regular (https://fonts.google.com/specimen/Pacifico). More fonts can be found here: https://fonts.google.com/.
fnt-siz | The font size that is used for text rendering [Integer]. Default: 72
fnt-sty | The font style that is used for text rendering [Integer; PLAIN, BOLD, ITALIC (0-2)]. Default: 0
sim-hue | The hue of the color of the simplex noise [Float, 0-1]. Default: none -> no simplex noise. "sim-hue:rdm" results in a random hue.
sim-seed | The seed used for the simplex noise texture [Integer]. Default: Random number from 1 to 1000.
sim-siz | The size of the noise intervals [Double]. Default: 150.
outl-col | The outline color in hexadecimal. Default: none (results in no outline).
outl-siz | The size of the outline [Float]. Default: 12.
outl-sty | The style of the outline [Integer]. Default: 0. Note: 0 = Solid, 1 = Dashed, 2 = Dotted.

Here are some example codes:
```bash
- {txt:Hello,fnt:"Pacifico-Regular"}
- {fnt-siz:120,txt:TEXT,bg-url:https://raw.githubusercontent.com/StylexTV/GSigns/master/showcase/hypixel.png}
- {txt:Bedwars,sim-hue:0.94}
```


## API

If you are a developer and want to use GSigns inside your plugin, for example to automatically create a sign, you can find the GSigns-API [here](https://github.com/StylexTV/GSigns-API).


## Project Layout

Here you can see the current structure of the project.

```bash
├─ 📂 showcase/       # ✨ Showcase (eg. for spigot)
├─ 📂 src/            # 🌟 Source Files
│  ├─ 📂 assets/          # ✒️ Plugin Assets
│  │  └─ 📂 color_tables  # 📦 Color Tables
│  ├─ 📂 de/stylextv/gs   # ✉️ Source Code
│  └─ 📄 plugin.yml       # 📌 Plugin-YML
├─ 📂 version/        # 📬 Versions (used by the auto-updater)
└─ 📃 readme.md       # 📖 Read Me!
```
