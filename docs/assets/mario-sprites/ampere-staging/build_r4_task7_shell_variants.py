from PIL import Image
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import mirror, tint

SRC = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


if __name__ == "__main__":
    shell = Image.open(f"{SRC}/turtle_shell.png").convert("RGBA")
    shell_dark = Image.open(f"{SRC}/turtle_shell_dark.png").convert("RGBA")

    # Red shell variant used by FlyingTurtlePatrol's projectile-death sprite.
    shell_red = tint(shell, (178, 74, 86), 0.45)

    # PackMarioAtlas source names (including original typo "Filp"):
    # TurtelShellRed.png, TurtelShellFilp.png, TurtelShellFilpdark.png,
    # TurtelShellFilpRed.png
    shell_red.save(f"{SRC}/TurtelShellRed.png")
    mirror(shell).save(f"{SRC}/TurtelShellFilp.png")
    mirror(shell_dark).save(f"{SRC}/TurtelShellFilpdark.png")
    mirror(shell_red).save(f"{SRC}/TurtelShellFilpRed.png")

    print("wrote turtle shell red + flipped variants")
