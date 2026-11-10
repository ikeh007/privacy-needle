/* Privacy Needle Calc cloud modeling engine. Client-only, no external requests. */
(function(root){
'use strict';
const catalog=root.PN_CLOUD_CATALOG;
if(!catalog)return;
const keys=Object.keys(catalog.providers);
const bound=(x,max=1e12)=>Number.isFinite(Number(x))?Math.min(max,Math.max(0,Number(x))):0;
const round=x=>Number.isFinite(x)?x:0;
const usd=x=>new Intl.NumberFormat('en-US',{style:'currency',currency:'USD',maximumFractionDigits:2}).format(round(x));
const fmt=x=>new Intl.NumberFormat('en-US',{maximumFractionDigits:2}).format(round(x));
const $=s=>document.querySelector(s), $$=s=>Array.from(document.querySelectorAll(s));
const val=(data,key,def=0)=>bound(data[key]??def);
const computeCost=(scenario,provider,editedRates,month=0)=>{
 const defaults=catalog.providers[provider]?.rates;
 if(!defaults)throw new Error('Unknown cloud provider');
 const r={...defaults,...(editedRates||{})};
 const growth=Math.pow(1+Math.min(500,val(scenario,'monthlyGrowth',0))/100,month);
 const regionalFactor=val(scenario,'regionalFactor',1)||1;
 const vm=val(scenario,'vm')*Math.min(744,val(scenario,'hours',730))*bound(r.compute)*regionalFactor;
 const db=val(scenario,'dbUnits')*Math.min(744,val(scenario,'dbHours',730))*bound(r.db)*regionalFactor;
 const object=val(scenario,'objectGB')*growth*bound(r.object);
 const block=val(scenario,'blockGB')*growth*bound(r.block);
 const egress=Math.max(0,val(scenario,'egressGB')*growth-bound(r.freeEgress))*bound(r.egress);
 const cross=val(scenario,'crossGB')*growth*bound(r.cross);
 const requests=Math.max(0,val(scenario,'requestsM')*growth-bound(r.freeRequests))*bound(r.functions);
 const base=vm+db+object+block+egress+cross+requests;
 const support=base*bound(r.support)/100;
 const contingency=(base+support)*val(scenario,'contingency',0)/100;
 const tax=(base+support+contingency)*val(scenario,'tax',0)/100;
 const parts={vm,db,object,block,egress,cross,requests,support,contingency,tax};
 return {provider,total:round(Object.values(parts).reduce((a,b)=>a+b,0)),parts,rate:r};
};
const normalize=data=>({
 vm:bound(data.vm??2),hours:bound(data.hours??730),dbUnits:bound(data.dbUnits??1),dbHours:bound(data.dbHours??730),
 objectGB:bound(data.objectGB??500),blockGB:bound(data.blockGB??150),egressGB:bound(data.egressGB??1500),
 crossGB:bound(data.crossGB??100),requestsM:bound(data.requestsM??3),monthlyGrowth:bound(data.monthlyGrowth??5),
 contingency:bound(data.contingency??0),tax:bound(data.tax??0),regionalFactor:bound(data.regionalFactor??1)
});
const costNames={vm:'Compute VMs',db:'Database compute',object:'Object storage',block:'Block storage',egress:'Internet egress',cross:'Cross-region transfer',requests:'Serverless request charges',support:'Support reserve',contingency:'Contingency',tax:'Illustrative tax'};
function h(tag,classes,text){const e=document.createElement(tag);if(classes)e.className=classes;if(text!==undefined)e.textContent=text;return e}
function td(text){return h('td','',String(text))}
function makeTable(head,rows){const table=h('table','datatable cloud-table'),thead=h('thead'),tr=h('tr');head.forEach(x=>tr.append(h('th','',x)));thead.append(tr);const body=h('tbody');rows.forEach(r=>{const tr=h('tr');r.forEach(x=>tr.append(td(x)));body.append(tr)});table.append(thead,body);return table}
function getScenario(){const s={};$$('#cloud-form [data-workload]').forEach(e=>{s[e.id]=e.value});return normalize(s)}
function getRates(){const result={};keys.forEach(provider=>{result[provider]={}; catalog.rateFields.forEach(([field])=>{const el=document.getElementById(`rate-${provider}-${field}`);if(el)result[provider][field]=bound(el.value)})});return result}
let report=null;
function render(){const el=$('#cloud-form');if(!el)return;
 const s=getScenario(),rates=getRates(),mode=el.dataset.mode,active=el.dataset.provider||'aws',selected=(mode==='compare'?keys:[active]);
 const data=selected.map(k=>computeCost(s,k,rates[k]));const ranked=[...data].sort((a,b)=>a.total-b.total),winner=ranked[0],max=Math.max(0.000001,...data.map(x=>x.total));
 const annual={}; selected.forEach(k=>{annual[k]=Array.from({length:12},(_,m)=>computeCost(s,k,rates[k],m).total).reduce((x,y)=>x+y,0)});
 const kpis=$('#cloud-kpis'); kpis.replaceChildren();
 const stats=mode==='compare'?
 [['Lowest modeled monthly',usd(winner.total)],['Lowest modeled provider',catalog.providers[winner.provider].short],['Highest minus lowest',usd(ranked.at(-1).total-winner.total)],['12-month forecast · lowest',usd(annual[winner.provider])]]:
 [['Monthly planning cost',usd(data[0].total)],['Baseline annual ×12',usd(data[0].total*12)],['12-month growth forecast',usd(annual[active])],['Modeled VM-hours',fmt(s.vm*Math.min(s.hours,744))]];
 stats.forEach(([label,value])=>{const div=h('div','kpi');div.append(h('span','number',value),h('span','key',label));kpis.append(div)});
 const bars=$('#cloud-bars');bars.replaceChildren();data.forEach(item=>{const p=catalog.providers[item.provider],row=h('div','provider-bar'),line=h('div','provider-bar-label'),name=h('span','',p.short),cost=h('strong','',usd(item.total)),track=h('div','provider-track'),fill=h('div','provider-fill');fill.style.width=`${Math.max(1,item.total/max*100)}%`;fill.style.setProperty('--provider-color',p.color);fill.setAttribute('role','img');fill.setAttribute('aria-label',`${p.short}: ${usd(item.total)} monthly`);line.append(name,cost);track.append(fill);row.append(line,track);bars.append(row)});
 const rows=Object.keys(costNames).map(k=>[costNames[k],...data.map(x=>usd(x.parts[k]))]);rows.push(['Total / month',...data.map(x=>usd(x.total))],['Annual cost · 12 months with variable growth',...data.map(x=>usd(annual[x.provider]))]);
 $('#cloud-table').replaceChildren(makeTable(['Monthly component',...data.map(x=>catalog.providers[x.provider].short)],rows));
 const forecast=[];[0,5,11].forEach(m=>forecast.push([`Month ${m+1}`,...data.map(x=>usd(computeCost(s,x.provider,rates[x.provider],m).total))]));
 $('#forecast-table').replaceChildren(makeTable(['Projection',...data.map(x=>catalog.providers[x.provider].short)],forecast));
 const insights=$('#cloud-insights');insights.replaceChildren();const tips=[];
 if(mode==='compare')tips.push(`Lowest modeled total: ${catalog.providers[winner.provider].name}. This is a planning result, not proof that the provider is faster, cheaper for every SKU, or a like-for-like production fit.`);
 if(s.egressGB>500)tips.push('Data transfer is material: check destination-based tiers, CDN/cache hit rates, inter-zone traffic and free allowances before procurement.');
 if(s.vm*s.hours>1000)tips.push('Reserved/committed compute may reduce sustained workloads. Discounts and purchase commitments are not included in this baseline.');
 if(s.requestsM>2)tips.push('Serverless charges here include request-count meters only. Execution duration, CPU, memory and provisioned concurrency are excluded.');
 if(s.monthlyGrowth>0)tips.push(`The 12-month forecast increases variable storage, egress and request usage by ${s.monthlyGrowth}% month over month. VM and database unit counts stay fixed.`);
 if(s.regionalFactor!==1)tips.push(`You applied a manual ${s.regionalFactor}× multiplier to VM and database hourly rates. This is not a verified regional provider quote.`);
 tips.push('Default rates are a combination of documented references and clearly marked illustrative estimates. Review each rate and official pricing page before relying on the totals.');
 tips.forEach(t=>insights.append(h('li','',t)));
 const sources=$('#cloud-price-sources');sources.replaceChildren();selected.forEach(k=>{const p=catalog.providers[k],section=h('div','source-record');section.append(h('strong','',`${p.name} · ${p.sku}`));const ref=h('div','source-links');p.references.forEach(([label,url])=>{const a=h('a','',label+' ↗');a.href=url;a.target='_blank';a.rel='noopener noreferrer';ref.append(a)});section.append(ref,h('p','subtle mini',p.caveat));sources.append(section)});
 report={s,rates,data,annual,selected,mode,active,updated:catalog.date};
 const state=$('#cloud-status');if(state)state.textContent='Calculated in your browser · USD reference assumptions · no account required';
 const fxRate=Number($('#fx-rate')?.value||1),currency=$('#currency')?.value||'USD';const conversion=$('#fx-conversion');if(conversion){if(currency!=='USD'&&fxRate>0&&Math.abs(fxRate-1)>.00001){conversion.textContent=`Manual conversion only: ${usd(winner.total)} ≈ ${new Intl.NumberFormat('en-US',{style:'currency',currency,maximumFractionDigits:2}).format(winner.total*fxRate)} at 1 USD = ${fmt(fxRate)} ${currency}. Rate supplied by you; no live FX feed.`}else conversion.textContent=currency==='USD'?'All estimates are displayed in USD.':'Enter a current USD → '+currency+' rate to see a converted reference. USD totals remain authoritative in this model.'}
}
function readURL(){const params=new URLSearchParams(location.search);$$('#cloud-form input[data-workload],#cloud-form select[data-workload],#cloud-form input[data-rate]').forEach(el=>{if(params.has(el.id)){const n=Number(params.get(el.id));if(Number.isFinite(n)&&n>=0&&n<=1e12)el.value=String(n)}});['currency','fx-rate'].forEach(key=>{const el=document.getElementById(key);if(el&&params.has(key)){const v=params.get(key);if(el.tagName==='SELECT'?Array.from(el.options).some(o=>o.value===v):Number.isFinite(Number(v))&&Number(v)>0)el.value=v}})}
function saveState(){const o={};$$('#cloud-form input,#cloud-form select,#currency,#fx-rate').forEach(el=>{if(el.id)o[el.id]=el.value});return o}
function restoreState(o){Object.entries(o||{}).forEach(([id,v])=>{const el=document.getElementById(id);if(el&&['INPUT','SELECT'].includes(el.tagName)){if(el.type==='number'&&(!Number.isFinite(Number(v))||Number(v)<0))return;el.value=String(v)}});render()}
function toast(msg){const el=$('#cloud-toast');if(!el)return;el.textContent=msg;el.setAttribute('role','status')}
function download(content,name,type){const blob=new Blob([content],{type}),a=document.createElement('a'),url=URL.createObjectURL(blob);a.href=url;a.download=name;document.body.append(a);a.click();a.remove();setTimeout(()=>URL.revokeObjectURL(url),1000)}
function exportCSV(){if(!report)return;const esc=v=>'"'+String(v).replace(/"/g,'""')+'"',lines=[['Privacy Needle Calc cloud estimate'],['Reference pricing date',report.updated],['Generated',new Date().toISOString()],['Unit','USD / month unless stated'],[],['Workload input','Value'],...Object.entries(report.s),[],['Cost component',...report.selected.map(k=>catalog.providers[k].name)],...Object.keys(costNames).map(k=>[costNames[k],...report.data.map(x=>x.parts[k].toFixed(4))]),['MONTHLY TOTAL',...report.data.map(x=>x.total.toFixed(4))],['12-MONTH GROWTH FORECAST',...report.selected.map(k=>report.annual[k].toFixed(4))],[],['Provider','Rate','Entered value','USD units','Verified reference?']];report.selected.forEach(k=>catalog.rateFields.forEach(([key,label,units])=>lines.push([k,label,report.rates[k]?.[key]??catalog.providers[k].rates[key],units,catalog.providers[k].verified.includes(key)?'Official baseline reference':'Illustrative / editable'])));lines.push([],['All outputs are estimates; no real-time prices or FX rates are fetched.']); download('\ufeff'+lines.map(r=>r.map(esc).join(',')).join('\r\n'),'privacy-needle-calc-cloud-estimate.csv','text/csv;charset=utf-8')}
function exportJSON(){if(!report)return;download(JSON.stringify({type:'Privacy Needle Calc estimate',methodology:'browser-side estimation',...report},null,2),'privacy-needle-calc-scenario.json','application/json')}
function init(){const form=$('#cloud-form');if(!form)return;readURL();const update=()=>{try{render()}catch(e){console.error(e);toast('Calculation error: check your inputs.')}};
 form.addEventListener('input',update);form.addEventListener('change',update);$('#currency')?.addEventListener('change',update);$('#fx-rate')?.addEventListener('input',update);
 $$('[data-workload-preset]').forEach(btn=>btn.addEventListener('click',()=>{const presets={starter:{vm:1,hours:220,dbUnits:0,dbHours:0,objectGB:50,blockGB:40,egressGB:150,crossGB:10,requestsM:.5,monthlyGrowth:3},growth:{vm:4,hours:730,dbUnits:1,dbHours:730,objectGB:1500,blockGB:350,egressGB:4500,crossGB:350,requestsM:12,monthlyGrowth:8},enterprise:{vm:24,hours:730,dbUnits:4,dbHours:730,objectGB:20000,blockGB:4000,egressGB:65000,crossGB:9000,requestsM:120,monthlyGrowth:10}};const preset=presets[btn.dataset.workloadPreset];if(preset)Object.entries(preset).forEach(([k,v])=>{const input=document.getElementById(k);if(input)input.value=v});update();toast('Workload preset applied. Edit any value.') }));
 $('#cloud-reset')?.addEventListener('click',()=>{form.reset();$('#fx-rate')&&($('#fx-rate').value='1');$('#currency')&&($('#currency').value='USD');history.replaceState(null,'',location.pathname);update();toast('Restored reference pricing and initial workload.')});
 $('#cloud-save')?.addEventListener('click',()=>{try{localStorage.setItem('pn-cloud-scenario-'+form.dataset.mode+'-'+(form.dataset.provider||'all'),JSON.stringify(saveState()));toast('Scenario saved locally in this browser.')}catch{toast('Storage unavailable; try a shared scenario link.')}});
 $('#cloud-load')?.addEventListener('click',()=>{try{const state=JSON.parse(localStorage.getItem('pn-cloud-scenario-'+form.dataset.mode+'-'+(form.dataset.provider||'all'))||'null');if(!state)return toast('No saved scenario on this browser.');restoreState(state);toast('Scenario loaded.')}catch{toast('Saved scenario unavailable.')}});
 $('#cloud-copy')?.addEventListener('click',async()=>{const params=new URLSearchParams;Object.entries(saveState()).forEach(([k,v])=>{if(v!=='')params.set(k,v)});const url=location.origin+location.pathname+'?'+params;try{await navigator.clipboard.writeText(url);toast('Share link copied. URL encodes the entered rates and usage; review before sharing.')}catch{window.prompt('Copy this share link',url)}});
 $('#cloud-export-csv')?.addEventListener('click',exportCSV);$('#cloud-export-json')?.addEventListener('click',exportJSON);$('#cloud-print')?.addEventListener('click',()=>window.print());
 $('#cloud-compare')?.addEventListener('click',()=>{const params=new URLSearchParams;Object.entries(saveState()).forEach(([k,v])=>{if(v!=='')params.set(k,v)});location.href='/compare/?'+params.toString()});
 update();
}
root.PNCloudCalc={computeCost,normalize};
if(typeof module!=='undefined'&&module.exports)module.exports={computeCost,normalize,catalog};
if(typeof document!=='undefined')document.addEventListener('DOMContentLoaded',init);
})(typeof window!=='undefined'?window:globalThis);
