"""
Step R.6 - Audio. Converts CC0 Kenney audio (local bundle at
C:/workspace/Kenney_Game_Assets_All/Audio, confirmed CC0 by reading its own
Readme.html - see KENNEY_ALL_IN_ONE_INDEX.md's own header note) into the
exact .wav files MarioResourceManager expects, and writes them directly into
app/src/main/assets/mario/audio/ - there's no reskin-overlay/packer step for
audio the way there is for sprites (MarioResourceManager.audioPath() just
does `"mario/audio/" + name + ".wav"`), so this script IS the swap, not a
staging step.

Format note (docs/mario/MARIO_RESKIN_EXECUTION.md Sec12.7's own open
question): current assets are mono 16-bit PCM WAV; Kenney's are OGG. No
ffmpeg is available in this environment, but `soundfile` (bundled libsndfile)
decodes OGG Vorbis directly, and its writer produces plain PCM WAV - a pure
per-file decode/re-encode, not a fragile shell-out. Every source file is
downmixed to mono (matching the existing convention) and written PCM_16.

Every pick below is a real file that exists in the local bundle (asserted at
run time, not assumed) - see the inline comment on each entry for why it was
chosen. Where multiple keys pull from the same effect family (e.g. the 3
impactMetal-based keys), a different numbered variant is used for each so
they don't sound identical.

Two keys are intentionally NOT touched: `smb_vine`/`smb_warning` are loaded
by MarioResourceManager but never played anywhere (dead code in the original
game too, confirmed in MARIO_GAME_MECHANICS.md Sec15.2) - left on the
original Nintendo-derived audio since replacing unreachable code has zero
player-visible effect.

Music mood assignment is a first pass based on title connotation plus a
lightweight loudness (RMS) check - NOT a real listen-through (no audio
playback available in this environment either). Flagged in
MARIO_RESKIN_EXECUTION.md as needing a human ear pass before shipping,
same caveat this whole doc set has carried for music since Sec2.6.
"""
import os
import soundfile as sf
import numpy as np

KENNEY = "C:/workspace/Kenney_Game_Assets_All/Audio"
OUT = "C:/workspace/morsecode/app/src/main/assets/mario/audio"


def convert(src_rel, out_name):
    src = os.path.join(KENNEY, src_rel)
    assert os.path.exists(src), f"missing source file: {src}"
    data, sr = sf.read(src, always_2d=False)
    if data.ndim > 1:
        data = data.mean(axis=1)  # downmix to mono, matching the existing asset convention
    out_path = os.path.join(OUT, out_name)
    sf.write(out_path, data, sr, subtype="PCM_16")
    print(f"wrote {out_name}  <-  {src_rel}  ({len(data)/sr:.2f}s, {sr}Hz, mono)")


# ------------------------------------------------------------- sound effects

SFX = {
    # jump: Digital Audio's own named-for-this phaseJump set - small vs.
    # super uses two different numbered takes so they're not identical.
    "smb_jump-small": "Digital Audio/Audio/phaseJump1.ogg",
    "smb_jump-super": "Digital Audio/Audio/phaseJump3.ogg",
    # coin: a short bright chime reads better than a "power up" swell.
    "smb_coin": "Digital Audio/Audio/highUp.ogg",
    # fireball/bowserfire: Sci-Fi Sounds' laser family - retro (small/thin)
    # for Ampere's own shot, large for the boss's.
    "smb_fireball": "Sci-Fi Sounds/Audio/laserRetro_000.ogg",
    "smb_bowserfire": "Sci-Fi Sounds/Audio/laserLarge_000.ogg",
    "smb_bowserfalls": "Sci-Fi Sounds/Audio/explosionCrunch_002.ogg",
    # breakblock/bump/kick: same impactMetal family (all three are "hit a
    # solid metal thing"), different numbered variants for some texture.
    "smb_breakblock": "Sci-Fi Sounds/Audio/impactMetal_000.ogg",
    "smb_bump": "Sci-Fi Sounds/Audio/impactMetal_002.ogg",
    "smb_kick": "Sci-Fi Sounds/Audio/impactMetal_004.ogg",
    "smb_stomp": "Impact Sounds/Audio/impactGeneric_light_000.ogg",
    # powerup/powerup_appears: a rising "shield/force-field" swell fits the
    # mechanical identity better than a generic synth blip.
    "smb_powerup": "Sci-Fi Sounds/Audio/forceField_000.ogg",
    "smb_powerup_appears": "Sci-Fi Sounds/Audio/forceField_002.ogg",
    "smb_pipe": "Sci-Fi Sounds/Audio/doorOpen_000.ogg",
    "smb_flagpole": "Digital Audio/Audio/phaserDown1.ogg",
    "smb_pause": "UI Audio/Audio/switch1.ogg",
    # 1-up/gameover/stage_clear/world_clear/mariodie/fireworks: all 6 are
    # short fanfare-style one-shots - Music Jingles' Retro set covers all of
    # them with distinct indices (per KENNEY_ALL_IN_ONE_INDEX.md Sec9.2's
    # own recommendation), rather than composing/finding 6 separate stingers.
    "smb_1-up": "Music Jingles/Audio (Retro)/jingles-retro_00.ogg",
    "smb_gameover": "Music Jingles/Audio (Retro)/jingles-retro_01.ogg",
    "smb_stage_clear": "Music Jingles/Audio (Retro)/jingles-retro_02.ogg",
    "smb_world_clear": "Music Jingles/Audio (Retro)/jingles-retro_03.ogg",
    "smb_mariodie": "Music Jingles/Audio (Retro)/jingles-retro_04.ogg",
    "smb_fireworks": "Music Jingles/Audio (Retro)/jingles-retro_05.ogg",
    # smb_vine / smb_warning deliberately omitted - dead code, see this
    # module's own docstring.
}

# ------------------------------------------------------------------- music

MUSIC = {
    # Ground (Surface) - the most-heard track by far; Music Loops/Retro is
    # the pack KENNEY_ALL_IN_ONE_INDEX.md Sec9.1 flags as the best chiptune-
    # identity fit, checked first per its own recommendation.
    "Ground": "Music Loops/Retro/Retro Beat.ogg",
    # UnderGround (Substrate) - "Mystic" is the only title in the Retro set
    # that reads as underground/eerie rather than upbeat/comedic.
    "UnderGround": "Music Loops/Retro/Retro Mystic.ogg",
    # Castle (Fortress) - the Retro set's remaining 3 tracks (Comedy/Polka/
    # Reggae) are all lighthearted, a poor fit for boss-adjacent tension;
    # pulled from the larger general Loops pool instead (same fallback the
    # original research already reserved for exactly this situation).
    "Castle": "Music Loops/Loops/Infinite Descent.ogg",
    # Star (invincibility) - highest measured RMS loudness of the reasonable
    # candidates checked (0.157) plus an on-theme title for a "powered up"
    # sci-fi moment.
    "Star": "Music Loops/Loops/Space Cadet.ogg",
    # Sea (Flooded Sector) - title directly evokes water/flow.
    "Sea": "Music Loops/Loops/Flowing Rocks.ogg",
}


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    print("--- Sound effects ---")
    for key, src in SFX.items():
        convert(src, f"{key}.wav")
    print("--- Music ---")
    for key, src in MUSIC.items():
        convert(src, f"{key}.wav")
