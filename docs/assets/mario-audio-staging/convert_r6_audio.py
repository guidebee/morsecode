"""R.6 audio reskin: convert selected Kenney .ogg source files to .wav and
copy them into app/src/main/assets/mario/audio/, matching the exact filenames
that MarioResourceManager.audioPath() expects ("mario/audio/<name>.wav").

Source bundle: C:\\workspace\\Kenney_Game_Assets_All\\Audio\\ (not in repo).
Mapping follows docs/mario/KENNEY_ALL_IN_ONE_INDEX.md section 9.

Run: py -3 docs/assets/mario-audio-staging/convert_r6_audio.py
"""
import soundfile as sf

KENNEY = r"C:\workspace\Kenney_Game_Assets_All\Audio"
DEST = r"C:\workspace\morsecode\app\src\main\assets\mario\audio"

# name -> source .ogg path (relative to KENNEY root)
MAPPING = {
    # --- Music (looping tracks) ---
    "Ground": r"Music Loops\Retro\Retro Beat.ogg",
    "UnderGround": r"Music Loops\Retro\Retro Mystic.ogg",
    "Castle": r"Music Loops\Retro\Retro Polka.ogg",
    "Star": r"Music Loops\Retro\Retro Comedy.ogg",
    "Sea": r"Music Loops\Retro\Retro Reggae.ogg",

    # --- SFX ---
    "smb_1-up": r"Music Jingles\Audio (Retro)\jingles-retro_08.ogg",
    "smb_bowserfalls": r"Sci-Fi Sounds\Audio\explosionCrunch_002.ogg",
    "smb_bowserfire": r"Sci-Fi Sounds\Audio\laserLarge_001.ogg",
    "smb_breakblock": r"Sci-Fi Sounds\Audio\impactMetal_003.ogg",
    "smb_bump": r"Sci-Fi Sounds\Audio\impactMetal_000.ogg",
    "smb_coin": r"Digital Audio\Audio\highUp.ogg",
    "smb_fireball": r"Sci-Fi Sounds\Audio\laserRetro_000.ogg",
    "smb_fireworks": r"Music Jingles\Audio (Retro)\jingles-retro_00.ogg",
    "smb_flagpole": r"Digital Audio\Audio\phaserDown1.ogg",
    "smb_gameover": r"Music Jingles\Audio (Retro)\jingles-retro_09.ogg",
    "smb_jump-small": r"Digital Audio\Audio\phaseJump1.ogg",
    "smb_jump-super": r"Digital Audio\Audio\phaseJump4.ogg",
    "smb_kick": r"Sci-Fi Sounds\Audio\impactMetal_004.ogg",
    "smb_mariodie": r"Music Jingles\Audio (Retro)\jingles-retro_12.ogg",
    "smb_pause": r"UI Audio\Audio\switch1.ogg",
    "smb_pipe": r"Sci-Fi Sounds\Audio\doorOpen_000.ogg",
    "smb_powerup": r"Digital Audio\Audio\powerUp5.ogg",
    "smb_powerup_appears": r"Sci-Fi Sounds\Audio\forceField_002.ogg",
    "smb_stage_clear": r"Music Jingles\Audio (Retro)\jingles-retro_11.ogg",
    "smb_stomp": r"Impact Sounds\Audio\impactGeneric_light_002.ogg",
    "smb_world_clear": r"Music Jingles\Audio (Retro)\jingles-retro_16.ogg",

    # smb_vine / smb_warning intentionally omitted: dead code, never
    # played anywhere in the Java (confirmed via grep) - guide itself
    # notes these were dead in the original engine too.
}


def main():
    import os
    for dest_name, rel_src in MAPPING.items():
        src = os.path.join(KENNEY, rel_src)
        data, samplerate = sf.read(src)
        dest = os.path.join(DEST, dest_name + ".wav")
        sf.write(dest, data, samplerate, subtype="PCM_16")
        print(f"{dest_name}.wav  <-  {rel_src}  ({samplerate} Hz, {data.shape})")


if __name__ == "__main__":
    main()
