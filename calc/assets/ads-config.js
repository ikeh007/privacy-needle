/* Privacy Needle Calc — existing Privacy Needle Google AdSense configuration.
   Publisher and unit IDs previously provided by the site owner.
   Placements correspond to the four PRE-EXISTING positions in every content page.
   In-feed is recorded but not rendered: Google's native in-feed design needs
   its AdSense-provided layout key and a genuine compatible content feed.
   Load ad tags automatically on normal pages; GOOGLE handles location-aware
   consent requirements via a published certified CMP configured in AdSense
   Privacy & messaging. Never bypass consent requirements in regulated regions.
   Privacy-policy page excludes third-party ad tags and uses house promotions.
   Do not enable overlapping Auto Ads formats without testing the design.
*/
window.PN_ADS={
  enabled:true,
  publisherId:'ca-pub-7008705915557363',
  slots:{
    leaderboard:'5570467970', // DisplayNow: top leaderboards
    sidebar:'4830732768',     // Right Skyscraper
    sidebarAlternate:'9582339877', // Left Skyscraper (other content pages)
    inarticle:'6472486103',   // In-Article
    footer:'2311392589',      // Multiplex native recommendation unit
    infeed:'7507872775'      // Reserved: requires native in-feed layout key
  },
  autoLoad:true,
  googleCmpManaged:true
};
