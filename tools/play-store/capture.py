"""Capture actual Android UI for the store listing; only convert PNG to RGB."""
import argparse
import io
import os
import re
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / 'docs/play-store/en-US/screenshots'


def adb(*args):
    serial = os.environ.get('ANDROID_SERIAL')
    selector = ['-s', serial] if serial else []
    return subprocess.check_output(['adb', *selector, *args])


def nodes():
    adb('shell', 'uiautomator', 'dump', '--compressed', '/sdcard/morse-store-ui.xml')
    return ET.fromstring(adb('shell', 'cat', '/sdcard/morse-store-ui.xml')).iter('node')


def bounds(node):
    return list(map(int, re.findall(r'\d+', node.attrib['bounds'])))


parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('action', choices=['dump', 'tap', 'capture'])
parser.add_argument('value', nargs='?')
parser.add_argument('--device-type', choices=['phone', 'tablet'], default='phone')
args = parser.parse_args()

if args.action == 'dump':
    for node in nodes():
        label = node.get('text') or node.get('content-desc')
        if label:
            print(f"{label!r} {node.get('bounds')} clickable={node.get('clickable')}")
elif args.action == 'tap':
    matches = [node for node in nodes()
               if args.value in (node.get('text'), node.get('content-desc'))]
    if len(matches) != 1:
        raise SystemExit(f'Expected one match for {args.value!r}, found {len(matches)}')
    x1, y1, x2, y2 = bounds(matches[0])
    adb('shell', 'input', 'tap', str((x1 + x2) // 2), str((y1 + y2) // 2))
else:
    if not args.value or not re.fullmatch(r'[a-z0-9-]+', args.value):
        raise SystemExit('Use a simple screenshot name, e.g. 01-home')
    activity = adb('shell', 'dumpsys', 'activity', 'activities').decode(errors='replace')
    resumed = [line for line in activity.splitlines() if 'topResumedActivity=' in line]
    if not any('au.com.guidebee.morsetool/' in line for line in resumed):
        raise SystemExit('Morse Code Toolkit must be the foreground app')
    screenshot = Image.open(io.BytesIO(adb('exec-out', 'screencap', '-p')))
    expected = [(1080, 1920)] if args.device_type == 'phone' else [(1920, 1200), (1200, 1920)]
    if screenshot.size not in expected:
        raise SystemExit(f'Expected one of {expected}, got {screenshot.size}')
    output = OUTPUT if args.device_type == 'phone' else OUTPUT.parent / 'tablet-screenshots'
    output.mkdir(parents=True, exist_ok=True)
    target = output / f'{args.value}.png'
    screenshot.convert('RGB').save(target, optimize=True)
    print(f'{target}: {screenshot.width}x{screenshot.height} RGB, {target.stat().st_size} bytes')
