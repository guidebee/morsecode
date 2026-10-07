"""Validate and package the English Play Store listing assets."""
import html
import argparse
import json
import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'docs/play-store/en-US'
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--tablet', action='store_true', help='Package only the Small_Tablet captures')
args = parser.parse_args()
short = (ASSETS / 'short-description.txt').read_text(encoding='utf-8').strip()
full = (ASSETS / 'full-description.txt').read_text(encoding='utf-8').strip()
assert 0 < len(short) <= 80, f'Short description: {len(short)} characters'
assert 0 < len(full) <= 4000, f'Full description: {len(full)} characters'
image_folder = 'tablet-screenshots' if args.tablet else 'screenshots'
screenshots = sorted((ASSETS / image_folder).glob('*.png'))
assert len(screenshots) == 8, f'Expected 8 screenshots, got {len(screenshots)}'
titles = ['Home dashboard', 'Audio decoder', 'Koch Trainer', 'Send Practice',
          'Transmit drills', 'Receive drills', 'Flashcards', 'Content Library']
if args.tablet:
    titles = ['Koch Trainer', 'Send Practice', 'Transmit drills', 'Receive drills',
              'Audio decoder', 'Flashcards', 'Content Library', 'Handbook']
records = []
cards = []
for screenshot, title in zip(screenshots, titles):
    with Image.open(screenshot) as captured:
        assert captured.format == 'PNG' and captured.mode == 'RGB', screenshot.name
        expected = [(1200, 1920), (1920, 1200)] if args.tablet else [(1080, 1920)]
        assert captured.size in expected, screenshot.name
        width, height = captured.size
        captured.verify()
    records.append({'file': screenshot.name, 'title': title, 'width': width,
                    'height': height, 'mode': 'RGB', 'bytes': screenshot.stat().st_size})
    src = f'{image_folder}/{screenshot.name}'
    cards.append(f'<figure><a href="{src}"><img src="{src}" alt="{title}" loading="lazy"></a>'
                 f'<figcaption>{title}<small>{screenshot.name}</small></figcaption></figure>')

preview = '''<!doctype html>
<html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Morse Code Toolkit - Play Store listing</title>
<style>
body{margin:0;background:#101216;color:#eef3fa;font:16px/1.6 system-ui,sans-serif}
main{max-width:1180px;margin:auto;padding:36px 24px}h1{font-size:34px;margin-bottom:8px}
a{color:#81beff}.intro{color:#b5c2d4}.copy{background:#1c222c;padding:24px;border-radius:16px;margin:24px 0}
pre{font:inherit;white-space:pre-wrap;margin:0}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(230px,1fr));gap:24px}
figure{margin:0}img{width:100%;height:auto;border-radius:12px;display:block}figcaption{margin:10px 0 24px}
small{display:block;color:#b5c2d4}summary{cursor:pointer;font-weight:600;margin-bottom:16px}
</style><main><h1>Morse Code Toolkit</h1><p class="intro">English store listing and eight actual DEVICE screenshots.
Click a screenshot to open the full-resolution PNG.</p>
<section class="copy"><h2>Short description</h2><p>SHORT_TEXT</p><small>SHORT_COUNT / 80 characters</small></section>
<details class="copy" open><summary>Full description - FULL_COUNT / 4,000 characters</summary><pre>FULL_TEXT</pre></details>
<h2>DEVICE screenshots - IMAGE_SIZE</h2><div class="grid">CARDS</div></main></html>'''
preview = preview.replace('SHORT_TEXT', html.escape(short)).replace('SHORT_COUNT', str(len(short)))
preview = preview.replace('FULL_TEXT', html.escape(full)).replace('FULL_COUNT', str(len(full)))
preview = preview.replace('CARDS', '\n'.join(cards))
preview = preview.replace('DEVICE', 'Small Tablet' if args.tablet else 'Phone')
preview = preview.replace('IMAGE_SIZE', f"{records[0]['width']} x {records[0]['height']}")
preview_name = 'tablet-index.html' if args.tablet else 'index.html'
validation_name = 'tablet-validation.json' if args.tablet else 'validation.json'
(ASSETS / preview_name).write_text(preview, encoding='utf-8')
manifest = {'shortDescriptionCharacters': len(short), 'fullDescriptionCharacters': len(full),
            'screenshots': records}
(ASSETS / validation_name).write_text(json.dumps(manifest, indent=2) + '\n', encoding='utf-8')
archive_name = 'morse-code-toolkit-small-tablet-en-US.zip' if args.tablet else 'morse-code-toolkit-listing-en-US.zip'
archive = ASSETS.parent / archive_name
files = screenshots + [ASSETS / preview_name, ASSETS / validation_name,
                        ASSETS / 'short-description.txt', ASSETS / 'full-description.txt',
                        ASSETS / ('TABLET_SCREENSHOTS.md' if args.tablet else 'README.md')]
with zipfile.ZipFile(archive, 'w', compression=zipfile.ZIP_DEFLATED) as bundle:
    for path in files:
        bundle.write(path, path.relative_to(ASSETS))
print(json.dumps({'shortCharacters': len(short), 'fullCharacters': len(full),
                  'screenshots': len(screenshots), 'archive': str(archive)}, indent=2))
