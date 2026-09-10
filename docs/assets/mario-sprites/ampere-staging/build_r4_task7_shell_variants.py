from PIL import Image
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import mirror, place_content, tint, verified_crop

SRC = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


if __name__ == "__main__":
    # Rebuild shell bases from the already-reskinned turtle strip so shell
    # occupancy better matches the gameplay-era silhouette (wide shell, low profile).
    turtle = Image.open(f"{SRC}/turtle.png").convert("RGBA")
    frame0 = verified_crop(turtle, (0, 0, 32, 48), "turtle shell frame0")
    shell_crop = verified_crop(frame0, (2, 12, 30, 46), "turtle shell body crop")
    shell = place_content(shell_crop, 32, 32, fill=1.08, anchor="bottom")
    shell_dark = tint(shell, (62, 86, 94), 0.34)

    shell.save(f"{SRC}/turtle_shell.png")
    shell_dark.save(f"{SRC}/turtle_shell_dark.png")

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
