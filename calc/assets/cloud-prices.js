/* Privacy Needle Calc — editable reference assumptions, reviewed 2026-11-10.
   This is not a live provider price feed. Rates marked 'illustrative' are modeling
   inputs, NOT verified SKU quotations. Refresh from official calculators before use. */
(function(root){
  'use strict';
  const providers={
    aws:{name:'Amazon Web Services',short:'AWS',color:'#f4ac45',sku:'EC2 t3.medium · Linux · 2 vCPU / 4 GiB · us-east-1',
      description:'Model EC2, RDS-style database capacity, S3 object storage, EBS block storage, Lambda invocation counts and network traffic.',
      source:'https://aws.amazon.com/pricing/',
      references:[['EC2 T3 reference SKU','https://aws.amazon.com/ec2/instance-types/t3/'],['Network and free transfer','https://aws.amazon.com/ec2/pricing/on-demand/'],['S3 object storage','https://aws.amazon.com/s3/pricing/'],['Lambda pricing','https://aws.amazon.com/lambda/pricing/'],['RDS pricing','https://aws.amazon.com/rds/pricing/']],
      rates:{compute:.0418,db:.075,object:.023,block:.08,egress:.09,freeEgress:100,cross:.02,functions:.20,freeRequests:1,support:0},
      verified:['compute','egress','freeEgress'],
      caveat:'T3 is burstable; CPU credits and performance vary. RDS DB, EBS, cross-region and Lambda execution costs depend on configuration. The current network model uses a flat first paid tier; it does not implement progressive tiers.'},
    azure:{name:'Microsoft Azure',short:'Azure',color:'#68baf6',sku:'B2s-style planning baseline · Linux · 2 vCPU / 4 GiB · East US',
      description:'Estimate Azure Virtual Machines, managed database capacity, Blob Storage, Managed Disks, Azure Functions and bandwidth.',
      source:'https://azure.microsoft.com/en-us/pricing/',
      references:[['Linux VMs','https://azure.microsoft.com/en-us/pricing/details/virtual-machines/linux/'],['Bandwidth','https://azure.microsoft.com/en-us/pricing/details/bandwidth/'],['Blob Storage','https://azure.microsoft.com/en-us/pricing/details/storage/blobs/'],['Azure Functions','https://azure.microsoft.com/en-us/pricing/details/functions/'],['Azure SQL','https://azure.microsoft.com/en-us/pricing/details/azure-sql-database/single/']],
      rates:{compute:.0416,db:.09,object:.0184,block:.075,egress:.087,freeEgress:100,cross:.02,functions:.20,freeRequests:1,support:0},
      verified:[],caveat:'All Azure default numeric rates are editable indicative assumptions, not an Azure Retail Prices API quote. B-series burstable capacity is not identical to other providers. Azure SQL and Functions execution may have additional meters.'},
    gcp:{name:'Google Cloud',short:'Google Cloud',color:'#84d5a0',sku:'E2 medium-style planning baseline · shared-core 2 vCPU / 4 GiB · us-central1',
      description:'Model Compute Engine, Cloud SQL-style database capacity, Cloud Storage, Persistent Disk, Cloud Run requests and internet egress.',
      source:'https://cloud.google.com/products/calculator',
      references:[['Compute Engine','https://cloud.google.com/compute/all-pricing'],['Network rates','https://cloud.google.com/vpc/network-pricing'],['Cloud Storage','https://cloud.google.com/storage/pricing'],['Cloud Run','https://cloud.google.com/run/pricing'],['Cloud SQL','https://cloud.google.com/sql/pricing']],
      rates:{compute:.0335,db:.087,object:.020,block:.04,egress:.12,freeEgress:1,cross:.02,functions:.40,freeRequests:2,support:0},
      verified:['object','egress','freeEgress','functions','freeRequests'],
      caveat:'E2-medium uses shared-core architecture, so the reference is not performance-equivalent to all other VMs. Google egress and Cloud Run have tiered billing, CPU/memory charges and location rules not captured by the simplified line-item model.'},
    oracle:{name:'Oracle Cloud Infrastructure',short:'Oracle Cloud',color:'#f08f87',sku:'VM.Standard.E4.Flex · 1 OCPU + 4 GB memory · x86',
      description:'Model OCI flexible compute, Autonomous Database-style capacity, Object Storage, Block Volumes, Functions and data transfer.',
      source:'https://www.oracle.com/cloud/price-list/',
      references:[['Flexible compute shapes','https://www.oracle.com/cloud/iaas-paas/'],['OCI official price list','https://www.oracle.com/cloud/price-list/'],['Storage','https://www.oracle.com/cloud/storage/pricing/'],['Networking','https://www.oracle.com/cloud/networking/pricing/'],['Functions','https://www.oracle.com/cloud/cloud-native/functions/']],
      rates:{compute:.0406286,db:.085,object:.0255,block:.0425,egress:.0085,freeEgress:10000,cross:.01,functions:.20,freeRequests:2,support:0},
      verified:['compute','freeEgress'],
      caveat:'1 OCI OCPU is not automatically the same CPU entitlement as competing VMs. E4 flexible compute is a sum of OCPU and RAM charges. The first 10 TB outbound allowance is drawn from the OCI price list; all unspecified workload meters are illustrative and editable.'}
  };
  const rateFields=[
    ['compute','VM compute','USD / VM-hour'],['db','Managed database compute','USD / DB-hour'],
    ['object','Object storage','USD / GB-month'],['block','Block storage','USD / GB-month'],
    ['egress','Internet egress','USD / GB'],['freeEgress','Included internet egress','GB / month'],
    ['cross','Cross-region transfer','USD / GB'],['functions','Serverless request charge','USD / 1M requests'],
    ['freeRequests','Included serverless requests','Million / month'],['support','Support allowance','% of subtotal']
  ];
  const date='2026-11-10';
  const catalog={providers,rateFields,date};
  root.PN_CLOUD_CATALOG=catalog;
  if(typeof module!=='undefined'&&module.exports)module.exports=catalog;
})(typeof window!=='undefined'?window:globalThis);
