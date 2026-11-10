'use strict';
const $=s=>document.querySelector(s),$$=s=>[...document.querySelectorAll(s)];
const fmt=(n,d=2)=>Number(n||0).toLocaleString('en-US',{minimumFractionDigits:d,maximumFractionDigits:d});
const money=n=>'$'+fmt(n,Math.abs(n)<1?4:2);
const num=(id,def=0)=>{const el=document.getElementById(id);const v=Number(el?.value);return Number.isFinite(v)?Math.max(0,v):def};
const val=(id,def='')=>document.getElementById(id)?.value||def;
const by=(id,text)=>{const el=document.getElementById(id);if(el)el.textContent=text};
const cap=(n,min=0,max=1e15)=>Math.min(max,Math.max(min,n));
const dailyMonth=30;
let currentReport={title:'Privacy Needle Cost Estimate',metrics:[],rows:[],assumptions:[]};
function output(metrics,rows=[],bars=[],assumptions=[]){currentReport={title:document.title,metrics,rows,assumptions};const k=$('#kpis'),b=$('#bars'),t=$('#result-table');if(!k||!b||!t)return;k.replaceChildren(...metrics.map(([label,value])=>{const x=document.createElement('div');x.className='kpi';const s=document.createElement('span');s.className='number';s.textContent=value;const l=document.createElement('span');l.className='key';l.textContent=label;x.append(s,l);return x}));b.replaceChildren();if(bars.length){const max=Math.max(0.001,...bars.map(x=>x[1]));bars.forEach(([name,value])=>{const r=document.createElement('div');r.className='barrow';const n=document.createElement('span');n.textContent=name;const track=document.createElement('div');track.className='bartrack';const fill=document.createElement('div');fill.className='barfill';fill.style.width=`${value/max*100}%`;track.append(fill);const price=document.createElement('strong');price.textContent=money(value);r.append(n,track,price);b.append(r)})}const table=document.createElement('table');table.className='datatable';const thead=document.createElement('thead');thead.innerHTML='<tr><th>Item</th><th>Monthly estimate (USD)</th></tr>';const tbody=document.createElement('tbody');rows.forEach(([name,value])=>{const tr=document.createElement('tr'),a=document.createElement('td'),c=document.createElement('td');a.textContent=name;c.textContent=typeof value==='number'?money(value):value;tr.append(a,c);tbody.append(tr)});table.append(thead,tbody);t.replaceChildren(table);by('result-state','Updated instantly · figures are estimates');}
function monthToLimit(current,limit,growth){if(current>=limit)return 'Already reached';if(current<=0)return 'Not determinable';if(growth<=0)return 'No growth assumed';const m=Math.ceil(Math.log(limit/current)/Math.log(1+growth/100));return m>240?'20+ years':`${m} month${m===1?'':'s'}`}
function aiCalc(){const req=num('requests',1000),days=cap(num('days',30),1,31),inWords=num('inwords',300),outWords=num('outwords',180),tWord=cap(num('tword',1.33),.1,5),cache=cap(num('cache',0),0,100)/100,images=num('images',0),imageRate=num('imageRate',.03);const inputTokens=req*days*inWords*tWord,outputTokens=req*days*outWords*tWord;
 const comparison=window.PN_PRICES.map(m=>{const input=inputTokens/1e6*(m.input*(1-cache)+(m.cached??m.input*0.1)*cache),output=outputTokens/1e6*m.output;return {...m,monthly:input+output,ip:input,op:output}}).sort((a,b)=>a.monthly-b.monthly);const selected=val('model','gpt-6-luna');const model=comparison.find(x=>x.id===selected)||comparison[0];const imageCost=images*days*imageRate;const monthly=model.monthly+imageCost;output([['Estimated monthly budget',money(monthly)],['Monthly input tokens',fmt(inputTokens,0)],['Monthly output tokens',fmt(outputTokens,0)]],[['Input tokens · '+model.model,model.ip],['Output tokens · '+model.model,model.op],['Images · custom rate (separate)',imageCost],['Total',monthly]],comparison.map(x=>[x.model,x.monthly]),[`Requests/day: ${req}`,`Days/month: ${days}`,`Input/output word-to-token multiplier: ${tWord}`,`Cached input percentage: ${(cache*100).toFixed(0)}%; charged at the model-specific cached-input rate where provided. Cache-write and storage charges excluded.`,`Images/day: ${images}, custom USD/image: ${imageRate}`]);const tbody=$('#comparison');if(tbody){tbody.replaceChildren(...comparison.map(x=>{const tr=document.createElement('tr');[x.provider+' · '+x.model,money(x.monthly),`${money(x.input)}/${money(x.output)}`].forEach(z=>{const td=document.createElement('td');td.textContent=z;tr.append(td)});return tr}))}by('model-source',model.src);if($('#model-source'))$('#model-source').href=model.src;const suggest=comparison[0];by('recommendation',`Lowest modeled text-only spend: ${suggest.model} (${money(suggest.monthly)}/month). Choose by suitability, quality, latency, data-handling and availability—not price alone. Local/open-weight hosting has separate GPU, operations and support costs.`)}
function dbCalc(){const choice=val('dbtype','d1'),gb=num('dbgb',2),growth=num('growth',10),reads=num('reads',800000),writes=num('writes',15000),dbs=Math.max(1,num('dbs',1)),indexes=num('indexes',20)/100,shards=Math.max(1,num('shards',1)),replicas=Math.max(1,num('replicas',1)),compute=num('compute',15),base=num('base',0),includedGB=num('includedGB',10),rateGB=num('rateGB',.25),readRate=num('readRate',0),writeRate=num('writeRate',0),rowBill=gb*(1+indexes);let rows,metrics,bars,notes;if(choice==='d1'){
  const storageExcess=Math.max(0,rowBill-5),billedStorage=storageExcess*.75,readBill=Math.max(0,reads*30-25e9)/1e6*.001,writeBill=Math.max(0,writes*30-50e6)/1e6*1,dbFee=Math.max(0,dbs-100000)*.1,monthly=compute+billedStorage+readBill+writeBill+dbFee;const violation=reads>5e6||writes>1e5||rowBill>5||dbs>10;metrics=[['D1 free-tier status',violation?'Over one or more limits':'Within modeled limits'],['Est. paid usage excess',money(monthly-compute)],['Storage incl. indexes',fmt(rowBill)+' GB']];rows=[['D1 reads over paid allowance',readBill],['D1 writes over paid allowance',writeBill],['D1 storage above 5 GB',billedStorage],['Workers/hosting base · user assumption',compute],['Total incremental plan estimate',monthly]];bars=[['Storage over paid tier',billedStorage],['Read usage over paid tier',readBill],['Write usage over paid tier',writeBill]];notes=[`FREE tier: ${reads>5e6?'OVER':'within'} daily read row cap; ${writes>1e5?'OVER':'within'} daily write row cap; ${rowBill>5?'OVER':'within'} 5GB aggregate storage; ${dbs>10?'OVER':'within'} 10-database allowance.`,`Storage could reach 5 GB in: ${monthToLimit(rowBill,5,growth)}.`,`Read rows and written rows are metered at query execution, not request counts; index use changes metered rows.`,`Paid D1 requires paid Workers plan; editable hosting base is an assumption, not an official charge.`,`Free-tier overflow is a hard limit, not an automatic fee.`];
 }else{const physical=rowBill*shards*replicas,storage=Math.max(0,physical-includedGB)*rateGB,readCost=reads*30/1e6*readRate,writeCost=writes*30/1e6*writeRate,infra=compute*shards*replicas+base,monthly=storage+readCost+writeCost+infra;metrics=[['Estimated monthly spend',money(monthly)],['Physical storage estimate',fmt(physical)+' GB'],['Compute instances',String(shards*replicas)]];rows=[['Compute / instances',infra],['Storage over allowance',storage],['Read-based fees · custom rate',readCost],['Write-based fees · custom rate',writeCost],['Total',monthly]];bars=[['Compute',infra],['Storage',storage],['Reads',readCost],['Writes',writeCost]];notes=['PostgreSQL and MySQL have no universal free tier. Enter your hosting provider’s actual quotas and rates.','Shard count, replicas and index overhead are modeled separately; backups and cross-region network traffic are excluded.'];}
 output(metrics,rows,bars,[`Database mode: ${choice}`,`Source: ${choice==='d1'?'https://developers.cloudflare.com/d1/platform/pricing/':'editable third-party assumptions'}`,...notes]);by('db-alert',notes.join(' '));}
const stackPresets={lean:{id:3,pass:2,endpoint:4,mdm:2,backup:2,training:1,secure:0,platform:20},standard:{id:6,pass:4,endpoint:7,mdm:5,backup:4,training:3,secure:4,platform:75},regulated:{id:12,pass:6,endpoint:13,mdm:10,backup:6,training:7,secure:9,platform:225}};
function stackCalc(){const staff=num('staff',30),contractors=num('contractors',5),seats=staff+contractors,region=val('region','global'),mode=val('stackPreset','standard'),setup=num('setup',400),devices=num('devices',60),annual=num('annual',10)/100;const prices=['id','pass','endpoint','mdm','backup','training','secure'].map(id=>({id,name:({id:'Identity & MFA',pass:'Password manager',endpoint:'Endpoint security',mdm:'Device management',backup:'Backup & recovery',training:'Security training',secure:'Secure access / VPN'})[id],price:num(id,0)}));let fixed=num('platform',75),sub=prices.reduce((a,x)=>a+x.price*seats,0)+fixed,monthlySetup=setup/12,monthlyDevice=devices*staff/36,discount=sub*annual,monthly=sub-discount+monthlySetup+monthlyDevice;output([['Fully loaded monthly',money(monthly)],['Cost per person / month',seats?money(monthly/seats):money(0)],['Estimated annual',money(monthly*12)]],[...prices.map(x=>[x.name,x.price*seats]),['Compliance / policy platform',fixed],['Licensing discount',-discount],['Onboarding amortized 12mo',monthlySetup],['Device replacement amortized 36mo',monthlyDevice],['Total',monthly]],prices.map(x=>[x.name,x.price*seats]),[`Region selected: ${region}; this changes guidance only, not product prices.`,`Preset: ${mode}; all per-seat pricing is illustrative and editable.`,`Discount assumed: ${annual*100}%.`,`Staff ${staff}; contractors ${contractors}.`]);const guide={africa:'Review Nigeria NDPA 2023, relevant African privacy regimes, cross-border transfer terms, device encryption and incident procedures.',eu:'Check GDPR data-processing terms, employee monitoring notices, international transfers and relevant national employment rules.',us:'Review applicable state privacy requirements, breach-notice timelines and company-specific contractual obligations.',global:'Map data transfers and contractor access by location; verify applicable laws and procurement requirements by country.'};by('regional-guide',guide[region]||guide.global)}
function egressCalc(){const gb=num('egressGB',2000),cache=num('cdnCache',30)/100,free=num('freeGB',100),rate=num('egressRate',.09),cross=num('crossGB',200),crossRate=num('crossRate',.02),flat=num('cdnFee',20),requests=num('requestsMil',2),requestRate=num('requestRate',0),effective=gb*(1-cache),billable=Math.max(0,effective-free),internet=billable*rate,regional=cross*crossRate,reqFee=requests*requestRate,total=internet+regional+flat+reqFee;output([['Total monthly transfer cost',money(total)],['Internet transfer billed',fmt(billable)+' GB'],['Saved via modeled cache',fmt(gb*cache)+' GB']],[['Internet egress',internet],['Cross-region traffic',regional],['CDN / fixed costs',flat],['Request fees',reqFee],['Total',total]],[['Internet egress',internet],['Cross-region',regional],['CDN fee',flat],['Requests',reqFee]],[`Estimated with editable provider prices. GB is treated as decimal gigabytes.`,`CDN hit ratio ${fmt(cache*100,0)}%; free internet egress ${free} GB/month.`,`Some providers have separate tiers, regions and CDN exclusions; use the vendor's price sheet.`])}
function seatCalc(){const workers=num('saasSeats',60),license=num('license',15),util=num('util',72)/100,growth=num('seatGrowth',20)/100,discount=num('seatDiscount',10)/100,tools=num('tools',6),annual=num('annualBilling',0)?12:12;const total=workers*license*tools*(1-discount),wasted=total*(1-util),used=total-wasted,future=total*(1+growth),savings=wasted*.5;output([['Monthly license cost',money(total)],['Unused licenses (modeled)',money(wasted)],['Potential savings at 50% reclamation',money(savings)]],[['Licenses serving active users (allocated)',used],['Licenses unused (allocated)',wasted],['Current total',total],['Future with seat growth',future]],[['Active allocation',used],['Unused allocation',wasted]],[`Illustrative all-tools-per-user pricing: ${tools} tools × ${workers} seats × ${money(license)} per seat.`,`Usage is an assumption; verify actual assignments from vendor exports.`,`Next-period seat growth assumed ${fmt(growth*100,0)}%; negotiated discount ${fmt(discount*100,0)}%.`])}
const calculators={ai:aiCalc,database:dbCalc,stack:stackCalc,egress:egressCalc,seats:seatCalc};
function initCalculator(){const form=$('[data-calculator]');if(!form)return;const type=form.dataset.calculator,fn=calculators[type];if(!fn)return;if(type==='ai'){const m=$('#model');window.PN_PRICES.forEach(x=>{const o=document.createElement('option');o.value=x.id;o.textContent=`${x.provider} · ${x.model}`;m.append(o)});m.value='gpt-6-luna'}const qs=new URLSearchParams(location.search);$$('[data-share]').forEach(input=>{const q=qs.get(input.id);if(q!==null){if(input.tagName==='SELECT'&&![...input.options].some(o=>o.value===q))return;input.value=q}});
 const update=()=>{try{fn()}catch(e){console.error(e);by('result-state','Please check the inputs.')}};form.addEventListener('input',update);form.addEventListener('change',update);const preset=$('#stackPreset');preset?.addEventListener('change',()=>{const p=stackPresets[preset.value];if(p){Object.entries(p).forEach(([key,value])=>{const e=document.getElementById(key);if(e)e.value=value})}update()});$$('input[type=range]').forEach(e=>{const target=document.querySelector(`[data-display-for="${e.id}"]`);if(target){const refresh=()=>target.textContent=e.value+'%';refresh();e.addEventListener('input',refresh)}});update();$('#reset-calculator')?.addEventListener('click',()=>{form.reset();if(type==='ai')$('#model').value='gpt-6-luna';history.replaceState(null,'',location.pathname);update()});$('#save-scenario')?.addEventListener('click',()=>{const values={};$$('[data-share]').forEach(e=>values[e.id]=e.value);localStorage.setItem('pn-scenario-'+type,JSON.stringify(values));flash('Scenario saved in this browser.')});$('#load-scenario')?.addEventListener('click',()=>{let data;try{data=JSON.parse(localStorage.getItem('pn-scenario-'+type)||'null')}catch{}if(!data)return flash('No locally saved scenario yet.');Object.entries(data).forEach(([key,v])=>{const el=document.getElementById(key);if(el)el.value=v});update();flash('Saved scenario restored.')});$('#share-calculator')?.addEventListener('click',async()=>{const params=new URLSearchParams;$$('[data-share]').forEach(e=>{if(e.value!=='')params.set(e.id,e.value)});const url=location.origin+location.pathname+'?'+params.toString();try{await navigator.clipboard.writeText(url);flash('Scenario URL copied.')}catch{prompt('Copy this scenario URL',url)}});$('#export-csv')?.addEventListener('click',()=>{const esc=s=>'"'+String(s).replaceAll('"','""')+'"';const lines=[['Privacy Needle estimate',currentReport.title],['Generated',new Date().toISOString()],[],['Metric','Value'],...currentReport.metrics,[],['Monthly cost item','USD'],...currentReport.rows,[],['Assumptions'],...currentReport.assumptions.map(x=>[x])];const csv='\ufeff'+lines.map(r=>r.map(esc).join(',')).join('\r\n');const blob=new Blob([csv],{type:'text/csv;charset=utf-8'});const a=document.createElement('a'),u=URL.createObjectURL(blob);a.href=u;a.download=`privacyneedle-${type}-estimate.csv`;a.click();setTimeout(()=>URL.revokeObjectURL(u),1000)});$('#print-estimate')?.addEventListener('click',()=>window.print())}
function flash(msg){let e=$('#toast');if(!e){e=document.createElement('div');e.id='toast';e.setAttribute('role','status');e.style.cssText='position:fixed;bottom:20px;left:50%;transform:translateX(-50%);padding:13px 18px;background:#eaca61;color:#141613;border-radius:9px;z-index:80;font-weight:700';document.body.append(e)}e.textContent=msg;setTimeout(()=>{if(e.textContent===msg)e.remove()},3300)}
function adsInit(){
 const ads=window.PN_ADS||{}, id=String(ads.publisherId||'');
 const configured=Boolean(ads.enabled&&/^ca-pub-\d{16}$/.test(id));
 // Google's published certified CMP, not a custom localStorage banner,
 // controls any consent that must be collected for advertising.
 // The former banner remains unshown in markup to preserve the design.
 const house={
  leaderboard:{label:'PRIVACY NEEDLE AI',title:'Privacy and compliance work, in one place.',sub:'Explore the AI-powered workspace for data protection teams.',url:'https://app.privacyneedle.com/',cta:'Explore Privacy Needle AI'},
  sidebar:{label:'PRIVACY NEEDLE SENTINEL',title:'Give your people stronger security instincts.',sub:'Discover practical awareness simulations and risk training.',url:'https://sentinel.privacyneedle.com/',cta:'Discover Sentinel'},
  inarticle:{label:'PRIVACY NEEDLE PLAY',title:'Turn cyber-awareness training into something people remember.',sub:'Explore interactive privacy and cybersecurity challenges.',url:'https://play.privacyneedle.com/',cta:'Explore free games'},
  footer:{label:'PRIVACY NEEDLE',title:'Independent news on privacy, AI and security.',sub:'Read the reporting and practical guides behind the products.',url:'https://privacyneedle.com/',cta:'Read Privacy Needle News'}
 };
 // First-party promotions appear until an eligible Google ad slot is requested.
 $$('.ad[data-ad-key]').forEach(el=>{
  const item=house[el.dataset.adKey];if(!item)return;
  const anchor=document.createElement('a');anchor.href=item.url;anchor.target='_blank';anchor.rel='noopener noreferrer';anchor.className='house-ad';
  const words=document.createElement('span');words.className='house-words';
  const eyebrow=document.createElement('span');eyebrow.className='house-eyebrow';eyebrow.textContent=item.label;
  const head=document.createElement('strong');head.textContent=item.title;
  const para=document.createElement('span');para.className='house-description';para.textContent=item.sub;
  const action=document.createElement('span');action.className='house-action';action.textContent=item.cta+' ↗';
  words.append(eyebrow,head,para);anchor.append(words,action);el.replaceChildren(anchor);
  el.classList.add('house-slot');el.setAttribute('aria-label','Privacy Needle product promotion');
 });
 let adScriptStarted=false;
 // The two skyscraper unit IDs are alternated across routes, not stacked.
 // This preserves the original sidebar count and positioning on each page.
 const path=location.pathname||'/';
 const alternateSidebar=Array.from(path).reduce((n,c)=>n+c.charCodeAt(0),0)%2===1;
 const slotFor=key=>{
  if(key==='sidebar'&&alternateSidebar)return ads.slots?.sidebarAlternate||ads.slots?.sidebar;
  return ads.slots?.[key];
 };
 const numericSlot=key=>/^\d{10}$/.test(String(slotFor(key)||''));
 function tryAds(){
  // The privacy notice is also the CMP privacy-policy URL: do not load ad tags there.
  const isPrivacyNotice=location.pathname.startsWith('/legal/privacy')||document.querySelector('link[rel=canonical]')?.getAttribute('href')==='https://calc.privacyneedle.com/legal/privacy/';
  if(!configured||adScriptStarted||isPrivacyNotice)return;
  const containers=$$('.ad[data-ad-key]').filter(el=>numericSlot(el.dataset.adKey));
  if(!containers.length)return;
  adScriptStarted=true;
  const script=document.createElement('script');script.async=true;
  script.crossOrigin='anonymous';
  script.src='https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client='+encodeURIComponent(id);
  script.onload=()=>{
   containers.forEach(el=>{
    const key=el.dataset.adKey, slot=slotFor(key);
    const label=document.createElement('span');label.textContent='Advertisement';label.className='adsense-label';
    const ins=document.createElement('ins');ins.className='adsbygoogle';ins.style.display='block';
    ins.dataset.adClient=id;ins.dataset.adSlot=String(slot);
    if(key==='inarticle'){
     // Correct markup for an AdSense In-Article ad unit.
     ins.style.textAlign='center';ins.dataset.adLayout='in-article';ins.dataset.adFormat='fluid';
    }else if(key==='footer'){
     // Multiplex units use the autorelaxed format.
     ins.dataset.adFormat='autorelaxed';
    }else if(key==='sidebar'){
     // Use responsive display format when the sidebar stacks on smaller screens.
     ins.dataset.adFormat=window.matchMedia('(min-width: 980px)').matches?'vertical':'auto';
     ins.dataset.fullWidthResponsive='true';
    }else{
     ins.dataset.adFormat=window.matchMedia('(min-width: 760px)').matches?'horizontal':'auto';
     ins.dataset.fullWidthResponsive='true';
    }
    el.replaceChildren(label,ins);el.classList.remove('house-slot');el.classList.add('real-ad');
    el.setAttribute('aria-label','Advertisement');
    try{(window.adsbygoogle=window.adsbygoogle||[]).push({})}
    catch(e){console.warn('AdSense could not initialize the '+key+' unit',e)}
   });
  };
  script.onerror=()=>{adScriptStarted=false;console.warn('AdSense blocked or unavailable; first-party promotions remain visible.')};
  document.head.append(script);
 }
 // A certified Google CMP must be published in AdSense Privacy & messaging.
 // It handles applicable region-specific consent and consent revocation.
 // Do not display the site's old non-certified preference dialog.
 $('#privacy-settings')?.addEventListener('click',event=>{
  if(window.googlefc&&typeof window.googlefc.showRevocationMessage==='function'){
   event.preventDefault();
   window.googlefc.showRevocationMessage();
  }
  // Otherwise the normal link opens the accurate Privacy & Advertising Notice.
 });
 tryAds();
}
document.addEventListener('DOMContentLoaded',()=>{const t=$('#menu-toggle');t?.addEventListener('click',()=>{const n=$('#navlinks');n?.classList.toggle('open');t.setAttribute('aria-expanded',n?.classList.contains('open')?'true':'false')});$('#year')&&(by('year',new Date().getFullYear()));adsInit();initCalculator()});
