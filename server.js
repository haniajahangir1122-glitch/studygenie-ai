const http = require('http');
const fs = require('fs');
const path = require('path');
const https = require('https');

const port = process.env.PORT || 3000;
const apiKey = process.env.GEMINI_API_KEY || "";

// Simple in-memory rate limiting map for security hardening
const ipRequests = new Map();
const RATE_LIMIT_WINDOW_MS = 60 * 1000; // 1 minute
const MAX_REQUESTS_PER_MINUTE = 60; // Max 60 requests per IP per minute

function checkRateLimit(req, res) {
  const ip = req.headers['x-forwarded-for'] || req.socket.remoteAddress || 'unknown';
  const now = Date.now();
  
  if (!ipRequests.has(ip)) {
    ipRequests.set(ip, []);
  }
  
  // Filter out expired timestamps
  const timestamps = ipRequests.get(ip).filter(t => now - t < RATE_LIMIT_WINDOW_MS);
  timestamps.push(now);
  ipRequests.set(ip, timestamps);
  
  if (timestamps.length > MAX_REQUESTS_PER_MINUTE) {
    res.statusCode = 429;
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify({ error: 'Too many requests. Please slow down and try again in a minute.' }));
    return false;
  }
  return true;
}

// Simple HTML escaping to prevent XSS (Cross-Site Scripting)
function escapeHtml(text) {
  if (typeof text !== 'string') return '';
  return text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

const server = http.createServer((req, res) => {
  const url = req.url;

  // Add robust security headers for public web application release
  res.setHeader('X-Frame-Options', 'DENY');
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-XSS-Protection', '1; mode=block');
  res.setHeader('Referrer-Policy', 'no-referrer');
  res.setHeader('Content-Security-Policy', "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:;");

  // --- API ROUTE: CONFIG ---
  if (url === '/api/config') {
    if (!checkRateLimit(req, res)) return;
    res.statusCode = 200;
    res.setHeader('Content-Type', 'application/json');
    const hasKey = apiKey.length > 0 && apiKey !== "MY_GEMINI_API_KEY";
    res.end(JSON.stringify({ hasApiKey: hasKey }));
    return;
  }

  // --- API ROUTE: CHAT (GEMINI SECURE FORWARDING) ---
  if (url === '/api/chat' && req.method === 'POST') {
    if (!checkRateLimit(req, res)) return;
    let body = '';
    req.on('data', chunk => {
      body += chunk.toString();
    });

    req.on('end', () => {
      try {
        const payload = JSON.parse(body);
        const rawPrompt = payload.prompt || '';
        
        // Sanitize and validate inputs
        const prompt = escapeHtml(rawPrompt).trim();
        if (!prompt) {
          res.statusCode = 400;
          res.setHeader('Content-Type', 'application/json');
          res.end(JSON.stringify({ error: 'Request body must contain a non-empty prompt.' }));
          return;
        }

        // If no API key configured, use intelligent offline/sandbox fallback response
        if (!apiKey || apiKey === "MY_GEMINI_API_KEY") {
          res.statusCode = 200;
          res.setHeader('Content-Type', 'application/json');
          res.end(JSON.stringify({ response: getFallbackResponse(prompt) }));
          return;
        }

        // Secure HTTPS Call to official Gemini API endpoint (gemini-3.5-flash)
        const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${apiKey}`;
        const requestData = JSON.stringify({
          contents: [{ parts: [{ text: prompt }] }],
          generationConfig: {
            temperature: 0.7,
            topP: 0.95
          },
          systemInstruction: {
            parts: [{ text: "You are StudyGenie, a friendly, ultra-knowledgeable AI study companion. Keep your explanations concise, structured, visually exciting, and focused on computer science and machine learning concepts. Use bold text, short bullet lists, and friendly terminology." }]
          }
        });

        const reqOptions = {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(requestData)
          }
        };

        const gReq = https.request(geminiUrl, reqOptions, (gRes) => {
          let gBody = '';
          gRes.on('data', d => { gBody += d; });
          gRes.on('end', () => {
            try {
              const gJson = JSON.parse(gBody);
              if (gJson.candidates && gJson.candidates[0] && gJson.candidates[0].content && gJson.candidates[0].content.parts[0]) {
                const aiText = gJson.candidates[0].content.parts[0].text;
                res.statusCode = 200;
                res.setHeader('Content-Type', 'application/json');
                res.end(JSON.stringify({ response: aiText }));
              } else {
                console.error('Unexpected Gemini structure:', gBody);
                res.statusCode = 200;
                res.setHeader('Content-Type', 'application/json');
                res.end(JSON.stringify({ response: "I encountered an API configuration discrepancy. Let me fall back to local mode: " + getFallbackResponse(prompt) }));
              }
            } catch (err) {
              res.statusCode = 500;
              res.end(JSON.stringify({ error: 'Failed parsing Gemini answer' }));
            }
          });
        });

        gReq.on('error', (err) => {
          console.error('Gemini connection error:', err);
          res.statusCode = 200;
          res.setHeader('Content-Type', 'application/json');
          res.end(JSON.stringify({ response: "Connection failed. Let me fall back to local mode: " + getFallbackResponse(prompt) }));
        });

        gReq.write(requestData);
        gReq.end();

      } catch (err) {
        res.statusCode = 400;
        res.end(JSON.stringify({ error: 'Invalid JSON request payload' }));
      }
    });
    return;
  }

  // --- STATIC FILE SERVING INTERFACES ---
  let filePath = '';
  let contentType = 'text/html';

  if (url === '/' || url === '/index.html') {
    filePath = path.join(__dirname, 'index.html');
    contentType = 'text/html';
  } else if (url === '/manifest.json') {
    filePath = path.join(__dirname, 'manifest.json');
    contentType = 'application/json';
  } else if (url === '/service-worker.js') {
    filePath = path.join(__dirname, 'service-worker.js');
    contentType = 'application/javascript';
  } else if (url === '/icon.svg') {
    filePath = path.join(__dirname, 'icon.svg');
    contentType = 'image/svg+xml';
  } else if (url === '/icon.jpg' || url === '/icon-192.png' || url === '/icon-512.png') {
    // Serve our beautifully generated high-resolution JPG app icon
    filePath = path.join(__dirname, 'src', 'assets', 'images', 'app_icon_1784178100637.jpg');
    contentType = 'image/jpeg';
  } else {
    // Basic 404 handler
    res.statusCode = 404;
    res.setHeader('Content-Type', 'text/plain');
    res.end('File Not Found');
    return;
  }

  // Read and pipe static files safely
  fs.readFile(filePath, (err, content) => {
    if (err) {
      console.error(`Error serving ${url}:`, err);
      res.statusCode = 500;
      res.setHeader('Content-Type', 'text/plain');
      res.end('Server Error serving asset');
    } else {
      res.statusCode = 200;
      res.setHeader('Content-Type', contentType);
      res.end(content);
    }
  });
});

// Intelligent, structured local assistant fallbacks
function getFallbackResponse(promptText) {
  const lower = promptText.toLowerCase();
  
  if (lower.includes('gradient')) {
    return "📊 <strong>Gradient Descent</strong> is an optimization algorithm that iteratively reduces cost by taking steps proportional to the negative gradient. Think of it like walking down a mountain at night: you feel the steepest slope under your feet and take a step in that direction until you reach the valley bottom!";
  } else if (lower.includes('network') || lower.includes('neural')) {
    return "🧠 <strong>Neural Networks Roadmap</strong>:<br><br>1. Understand Linear Regression & Perceptrons.<br>2. Study Activation Functions (ReLU, Sigmoid).<br>3. Master Backpropagation calculus (chain rule).<br>4. Build simple MLPs in Python/Kotlin.<br>5. Explore deep structures like CNNs or Transformer weights.";
  } else if (lower.includes('stress') || lower.includes('manage')) {
    return "🧘 <strong>Managing study stress</strong> is key to neuroplastic retention. Practice the 4-7-8 breathing method: inhale for 4 seconds, hold for 7, and exhale completely for 8. Space your StudyGenie cards into 25-minute Pomodoro clusters with green tea breaks.";
  } else if (lower.includes('hello') || lower.includes('hi')) {
    return "Hello Jane! I'm ready. Let me know if you want me to explain backpropagation, give you study guides, or load textbook indices!";
  } else {
    return `🧞 StudyGenie is active in local sandbox! I parsed your study query ("${promptText}"). To get a real-time, highly dynamic generative answer, insert a valid Gemini Key in your AI Studio Secrets panel!`;
  }
}

server.listen(port, '0.0.0.0', () => {
  console.log(`Server running on port ${port}`);
});
