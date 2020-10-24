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
  <a href="https://www.codacy.com/manual/noluck942/GSigns?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=StylexTV/GSigns&amp;utm_campaign=Badge_Grade">
    <img alt="Codacy Badge" src="https://app.codacy.com/project/badge/Grade/a33dbb19ff17460d896a7864fececab6"/>
  </a>
  <a href="https://stylextv.gitbook.io/gsigns" alt="Docs (gitbook)">
    <img src="https://img.shields.io/badge/docs-gitbook-brightgreen"/>
  </a>
  <a>
    <img alt="Code size" src="https://img.shields.io/github/languages/code-size/StylexTV/GSigns.svg"/>
  </a>
  <a>
    <img alt="GitHub repo size" src="https://img.shields.io/github/repo-size/StylexTV/GSigns.svg"/>
  </a>
  <a>
    <img alt="Lines of Code" src="https://tokei.rs/b1/github/StylexTV/GSigns?category=code"/>
  </a>
</p>

## What is it?
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
