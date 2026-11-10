"""Simple offline build audit; pip install beautifulsoup4 for use."""
from pathlib import Path
from bs4 import BeautifulSoup
from urllib.parse import urlparse
import json, xml.etree.ElementTree as ET
ROOT=Path(__file__).parent.parent
missing=[]
pages=sorted(ROOT.rglob('index.html'))
for page in pages:
 soup=BeautifulSoup(page.read_text(encoding='utf-8'),'html.parser')
 path='/'+str(page.relative_to(ROOT)).replace('index.html','')
 assert soup.title and soup.h1,(str(page),'title or h1 missing')
 canonical=soup.find('link',rel='canonical')
 assert canonical and canonical.get('href')=='https://calc.privacyneedle.com'+path,(str(page),'bad canonical')
 assert len(soup.select('meta[name=description]'))==1,(str(page),'description missing')
 keys={e.get('data-ad-key') for e in soup.select('.ad[data-ad-key]')}
 assert keys.issuperset({'leaderboard','inarticle','sidebar','footer'}),(str(page),'ad placements missing')
 for schema in soup.find_all('script',type='application/ld+json'):json.loads(schema.string)
 for e in soup.find_all(['a','script','link']):
  url=e.get('href') or e.get('src') or ''
  if not url.startswith('/') or url.startswith('//'):continue
  f=ROOT/urlparse(url).path.lstrip('/')
  if f.exists() or (f/'index.html').exists():continue
  missing.append((str(page),url))
assert not missing,missing[:20]
tree=ET.parse(ROOT/'sitemap.xml');urls=tree.findall('.//{*}loc');assert len(urls)==len(pages)
print(f'PASS: {len(pages)} indexable pages, four ad placements/page, structured data valid, internal assets/links found, sitemap correct.')
