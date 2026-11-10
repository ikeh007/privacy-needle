# Privacy Needle Calc — Cloud, AI & SaaS Cost Intelligence

**Deployment target:** https://calc.privacyneedle.com/

A complete, build-free Cloudflare Pages static site. **The ZIP does not itself create DNS, publish to Cloudflare, approve AdSense, or guarantee search indexation.** Launch those steps in the Cloudflare and Google dashboards after testing.

## What is included

**10 interactive tools** across 20 SEO-readable, separately navigable pages:

1. Four-provider AWS / Azure / Google Cloud / Oracle comparison `/compare/`;
2. AWS estimator `/cloud/aws/`;
3. Azure estimator `/cloud/azure/`;
4. Google Cloud estimator `/cloud/google-cloud/`;
5. Oracle Cloud estimator `/cloud/oracle-cloud/`;
6. AI Token API Budget Optimizer `/tools/ai-token-calculator/`;
7. Cloudflare D1 and custom PostgreSQL/MySQL estimator `/tools/cloud-database-calculator/`;
8. Remote-worker compliance calculator `/tools/remote-stack-calculator/`;
9. Cloud data egress estimator `/tools/cloud-egress-calculator/`;
10. SaaS license waste calculator `/tools/saas-license-calculator/`.

Also includes original research guides, two new cloud comparison/egress explainers, pricing sources, ecosystem directory, privacy and methodology pages, 404 page, `sitemap.xml`, `robots.txt`, `llms.txt`, manifest, icon and security headers.

**Cloud tools:** workload templates, custom compute/DB/storage/network/serverless line-item prices for each provider, editable free allowances, regional compute rate multiplier (manual), support reserve, tax and contingency, monthly/yearly comparison, 12-month variable-usage forecast, browser-side charts, local scenario save/load, shareable scenario inputs, CSV and JSON export, and browser Print/Save PDF.

**Cross-promotions:** Privacy Needle News, AI, Play, Privacy Score Checker, Sentinel, and Christmas appear in the sitewide product navigation, footer and promotional modules. The exact availability of Sentinel and Christmas destinations should be checked before production promotion.

## Deploy to Cloudflare Pages

1. **Extract this ZIP.** The files `index.html`, `assets/`, `cloud/`, `compare/`, etc., must be at the root of the deployed directory, not nested under another folder.
2. Go to [Cloudflare Workers & Pages](https://dash.cloudflare.com/?to=/:account/workers-and-pages), create a **Pages** project and choose **Direct Upload**. Upload the extracted folder or ZIP.
3. Set no build command, no framework and deploy the static root. Check the generated `*.pages.dev` address.
4. Test `/`, `/compare/`, all provider calculators, specialist calculators, links, CSV export, the model rate editor, and mobile layout. Use one sample workload and confirm changing a rate moves the calculation.
5. In the Pages project, use **Custom Domains** → Add `calc.privacyneedle.com`, and follow Cloudflare's required DNS records. Do not modify `privacyneedle.com` WordPress, existing SSL, AdSense tags or other working subdomains.
6. Verify the site is publicly accessible on HTTPS and that `/robots.txt` and `/sitemap.xml` both return HTTP 200. Submit `https://calc.privacyneedle.com/sitemap.xml` to [Google Search Console](https://search.google.com/search-console/) and Bing Webmaster Tools. Search engines choose what and when to index, even with correct files.
7. For continuous publishing, consider Cloudflare Pages GitHub integration instead of Direct Upload at initial project creation. Cloudflare documents that Direct Upload projects cannot subsequently switch to Git integration.

## Advertising across every content page — configured with existing publisher units

**The real Google AdSense publisher and five compatible existing unit IDs are configured for automatic requests on normal pages.** This update leaves all 20 content-page layouts and all 80 original advertisement positions in place; no new containers or changed design styles were added. The original Privacy Needle product promotions remain fallback content if Google ads are blocked, unavailable or intentionally disabled.

Existing account: `ca-pub-7008705915557363`; authorized seller declaration: `pub-7008705915557363`. An invisible Google AdSense ownership meta tag is included on all 20 content pages. The included `/ads.txt` serves the declaration on `calc.privacyneedle.com` after deployment. Do not overwrite `privacyneedle.com/ads.txt`; confirm root and subdomain crawler setup in Google AdSense.

| Existing page position | Existing unit name | Ad unit ID | Google format |
|---|---|---|---|
| Leaderboard | DisplayNow | `5570467970` | Responsive display, horizontal desktop |
| Sidebar (alternate routes) | Right Skyscraper | `4830732768` | Vertical desktop, responsive mobile |
| Sidebar (alternate routes) | Left Skyscraper | `9582339877` | Vertical desktop, responsive mobile |
| In-content/in-article | In-Article | `6472486103` | `fluid`, `in-article` |
| Footer | Multiplex | `2311392589` | `autorelaxed` |
| Reserved for a future native card feed | In-Feed | `7507872775` | **Not rendered**: requires matching layout key/code from AdSense |

On the 19 standard content pages, four existing placements now automatically request ads on page load; the Google-certified CMP must handle consent where required. The privacy notice page remains free of third-party ad tags so it can be used as the consent-message privacy policy URL, and displays house promotions only. The 404 page has no ads. No fake wait, hidden click target, refresh loop, obstructed form or additional visual zone was added.

**Before production:** Confirm `calc.privacyneedle.com` is accepted by AdSense and the unit IDs are active for the account. **Required launch step:** Publish a Google-certified CMP European regulations message (IAB TCF) for `calc.privacyneedle.com` from AdSense > Privacy & messaging **before launching paid ads to EEA, UK and Swiss visitors**. The old, non-certified opt-in banner is no longer shown. A properly configured Google CMP displays required notices and follows visitor choices; it may serve limited ads where permitted without cookie-based consent, but paid inventory is not guaranteed. Configure any applicable US state opt-out messages too. Verify that the current account Auto Ads settings do not insert additional units or overlays that change the carefully designed layout. Test the live site's console and Google Policy Center; ad fill and review approval are not guaranteed.

Google help: https://support.google.com/adsense/answer/13554116

## Cloud pricing and audit requirements

**Cloud rates are not live pricing.** `assets/cloud-prices.js` contains reference values reviewed **10 November 2026**, with a `verified` array per provider naming which baseline fields were checked against official sources. Other default numeric inputs are **illustrative and must be verified** with the provider before procurement decisions.

- AWS reference: EC2 t3.medium Linux, US East, official example EC2 compute and internet transfer allowance/tier. Other rates editable.
- Azure reference: B2s-like 2 vCPU / 4 GiB planning baseline. **All Azure numeric fields are illustrative.** Verify actual region and pay-as-you-go/contracted SKU in Azure's pricing calculator.
- Google Cloud reference: E2-medium-like shared-core planning example; official reference for a standard storage starting price, first Premium network tier, and Cloud Run request meter/free allowance. Other rates editable.
- Oracle reference: OCI VM.Standard.E4.Flex (1 OCPU + 4 GB RAM), priced using the official OCPU and memory rates. First 10 TB free outbound is modeled conservatively as 10,000 GB; actual unit/eligibility can differ, and later paid rate is illustrative.

The underlying compute profiles have **different performance and baseline entitlements** and are not equivalent benchmarks. The 10-component modeled bill also excludes many potential invoice items: serverless CPU time/duration, storage operations, NAT gateways, API Gateway, backups/snapshots, logging, reserved capacity, taxes unless entered, support entitlements, licenses, service-specific free-limit complexities, progressive bandwidth tiers, and vendor-negotiated discounts. The regional multiplier is manually supplied and is not an automatic region-specific price lookup. Source links are visible in all cloud calculators and the `/pricing-sources/` register.

For AI model rates see `assets/pricing.js` and the linked provider pricing pages. Token estimates approximate words-to-token ratios and cached-token billing and are not a substitute for exact tokenization or vendor billing.

## SEO / GEO / AEO / reliability

- 20 fully rendered HTML pages, stable descriptive URLs, one title, one H1 and a unique description on each page, canonical to the target subdomain.
- Schema.org JSON-LD with `SoftwareApplication` for the calculators, BreadcrumbList, WebSite or WebPage, plus FAQPage when a visible FAQ exists.
- Native HTML content, FAQs, official source citations, guides and editorial-style methodology. AI answer engines may find these more readable than a client-only SPA shell. `llms.txt` is informational only, not an indexing guarantee.
- Sitemap and robots are preconfigured for **`calc.privacyneedle.com`**, so deploy to that host or update all absolute canonical and sitemap URLs before using another one.
- Fully client-side arithmetic: there are no Cloudflare Worker, D1, API key, billing service or database dependencies for the calculators. Browser localStorage is used only for explicit scenario saves; Google controls any applicable ad consent storage. Sharing scenario URLs encodes input values, so review them before sending.
- CSS responsive layout, focus-visible controls, labels, accessible chart text alternatives, local and print-friendly reports.

## Running tests

- `node --check assets/app.js`
- `node --check assets/pricing.js`
- `node --check assets/cloud-prices.js`
- `node --check assets/cloud-engine.js`
- `node --test tests/cloud-math.test.js`
- `python tests/site-audit.py`
- `python -m http.server 8000` and browse http://localhost:8000 to test normal routing; the `*.pages.dev` preview is the final browser check.

## Owner operations

- **Pricing maintenance:** update the price data source JS and the review date after verification; verify source links at least monthly and after major vendor pricing revisions.
- **Sponsorships:** maintain editorial separation between paid placements and calculation results. Never adjust estimates to favour sponsors.
- **Product links:** check Sentinel and Christmas subdomain live status and revise links accordingly.
- **Google Search Console:** submit sitemap and inspect any crawl/quality issues; indexation, impressions, CPC and AdSense approval are not guaranteed.

Owner contact: info@privacyneedle.com · https://privacyneedle.com/

## v4 change log — auto ads, same design

- Existing homepage, calculators, UI components and CSS are unchanged.
- Google AdSense script now requests the existing four ad units automatically on each of 19 normal HTML pages, without requiring everyone to click a local opt-in banner.
- Consent for EEA/UK/Swiss traffic must be handled in Google AdSense Privacy & messaging using a **published, certified CMP**. Non-personalized ads can also use cookies; do not disable required consent checks. Google offers eligible limited ads in certain no-consent cases.
- Footer Advertising preferences opens Google revocation UI if available; otherwise the Privacy & Advertising Notice.
- Google ad tags are suppressed **only on the privacy-notice page** so that the notice can be used in your certified CMP. The same visual ad placements still display first-party promotions there.
- Unfilled ads, ad blockers, account review and user choices may prevent paid ads; never promise ads on every pageview.

Official guidance:
- https://support.google.com/adsense/answer/13554116
- https://support.google.com/adsense/answer/14210870
- https://developers.google.com/funding-choices/fc-api-docs
- https://support.google.com/adsense/answer/10961370
