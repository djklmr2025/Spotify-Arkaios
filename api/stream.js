// Vercel Serverless Function to stream Google Drive audio with HTTP 206, Range requests, and CORS
import { Readable } from 'stream';

export default async function handler(req, res) {
  const { id } = req.query;

  if (!id || typeof id !== 'string') {
    return res.status(400).json({ error: 'Falta el parámetro id del track de Google Drive' });
  }

  // Preflight CORS
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, HEAD, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Range, Content-Type, Accept');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  const driveUrl = `https://drive.usercontent.google.com/download?id=${encodeURIComponent(id)}&export=download`;

  const requestHeaders = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Accept': '*/*'
  };

  if (req.headers.range) {
    requestHeaders['Range'] = req.headers.range;
  }

  try {
    const upstream = await fetch(driveUrl, {
      method: 'GET',
      headers: requestHeaders
    });

    res.status(upstream.status);

    const forwardHeaders = [
      'content-type',
      'content-length',
      'content-range',
      'accept-ranges',
      'last-modified',
      'etag'
    ];

    forwardHeaders.forEach(headerName => {
      const val = upstream.headers.get(headerName);
      if (val) {
        res.setHeader(headerName, val);
      }
    });

    if (!res.getHeader('content-type')) {
      res.setHeader('Content-Type', 'audio/mpeg');
    }

    res.setHeader('Cache-Control', 'public, max-age=86400, s-maxage=86400');

    if (!upstream.body) {
      return res.end();
    }

    const nodeStream = Readable.fromWeb(upstream.body);
    nodeStream.on('error', (err) => {
      console.error('Streaming pipeline error:', err);
      if (!res.headersSent) res.status(500).end();
    });

    nodeStream.pipe(res);
  } catch (err) {
    console.error('Serverless stream error:', err);
    if (!res.headersSent) {
      res.status(500).json({ error: 'Error al conectar con el servidor de audio de Google Drive' });
    }
  }
}
