/**
 * Real YouTube Channel Playlists & Videos Extractor for @Infraction
 * Uses YouTube's official public page responses without third party services.
 */
const https = require('https');
const fs = require('fs');
const path = require('path');

function fetchPage(url) {
  return new Promise((resolve, reject) => {
    const parsed = new URL(url);
    const options = {
      hostname: parsed.hostname,
      path: parsed.pathname + parsed.search,
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Accept-Language': 'en-US,en;q=0.9',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
      }
    };
    https.get(options, (res) => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        return fetchPage(res.headers.location).then(resolve).catch(reject);
      }
      let html = '';
      res.on('data', chunk => html += chunk);
      res.on('end', () => resolve(html));
    }).on('error', reject);
  });
}

function extractInitialData(html) {
  const match = html.match(/var ytInitialData = ({.*?});<\/script>/s) ||
                html.match(/window\["ytInitialData"\] = ({.*?});<\/script>/s);
  if (!match) return null;
  try {
    return JSON.parse(match[1]);
  } catch (err) {
    return null;
  }
}

async function main() {
  console.log('[1/3] Fetching https://www.youtube.com/@Infraction/playlists ...');
  const html = await fetchPage('https://www.youtube.com/@Infraction/playlists');
  const data = extractInitialData(html);
  if (!data) {
    console.error('Failed to extract ytInitialData from playlists page');
    process.exit(1);
  }

  // Find Playlists tab
  const tabs = data?.contents?.twoColumnBrowseResultsRenderer?.tabs || [];
  const playlistTab = tabs.find(t => t.tabRenderer && (t.tabRenderer.title === 'Playlists' || t.tabRenderer.selected));

  let items = [];
  const sectionContents = playlistTab?.tabRenderer?.content?.sectionListRenderer?.contents || [];
  for (const sec of sectionContents) {
    const gridItems = sec?.itemSectionRenderer?.contents?.[0]?.gridRenderer?.items || [];
    if (gridItems.length > 0) {
      items = items.concat(gridItems);
    }
  }

  console.log(`Found ${items.length} playlist items on first page of @Infraction/playlists.`);

  const parsedPlaylists = [];
  for (const item of items) {
    const lockup = item.lockupViewModel;
    if (!lockup) continue;
    const playlistId = lockup.contentId;
    const title = lockup.metadata?.lockupMetadataViewModel?.title?.content;
    const thumbnailSources = lockup.contentImage?.collectionThumbnailViewModel?.primaryThumbnail?.thumbnailViewModel?.image?.sources || [];
    const thumbUrl = thumbnailSources[thumbnailSources.length - 1]?.url;
    const videoCountBadge = lockup.contentImage?.collectionThumbnailViewModel?.primaryThumbnail?.thumbnailViewModel?.overlays?.[0]?.thumbnailOverlayBadgeViewModel?.thumbnailBadges?.[0]?.thumbnailBadgeViewModel?.text;
    const leadVideoId = lockup.itemPlayback?.inlinePlayerData?.onSelect?.innertubeCommand?.watchEndpoint?.videoId;

    parsedPlaylists.push({
      playlistId,
      title,
      videoCountBadge,
      thumbUrl,
      leadVideoId,
      url: `https://www.youtube.com/playlist?list=${playlistId}`,
      embedUrl: `https://www.youtube-nocookie.com/embed/videoseries?list=${playlistId}`,
      channel: 'Infraction - No Copyright Music',
      channelUrl: 'https://www.youtube.com/@Infraction',
      verified: true
    });
  }

  console.log('\n--- Sample Real Playlists from @Infraction ---');
  parsedPlaylists.slice(0, 10).forEach((p, idx) => {
    console.log(`${idx + 1}. [${p.playlistId}] "${p.title}" (${p.videoCountBadge}) - Lead Video: ${p.leadVideoId}`);
  });

  fs.writeFileSync(path.join(__dirname, 'real_channel_playlists_sample.json'), JSON.stringify(parsedPlaylists, null, 2));
  console.log(`\nSaved ${parsedPlaylists.length} real playlists to scripts/real_channel_playlists_sample.json`);
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});
