import { Hono } from 'hono';

const app = new Hono();

// Get referral code
app.get('/code', async (c) => {
  try {
    const token = c.req.header('Authorization')?.replace('Bearer ', '');
    if (!token) return c.json({ error: 'Unauthorized' }, 401);

    const uid = Buffer.from(token, 'base64').toString().split(':')[0];

    // Get or generate referral code
    let code = await c.env.KV_CACHE.get(`ref_code:${uid}`);
    
    if (!code) {
      code = Buffer.from(uid).toString('base64').slice(0, 8).toUpperCase();
      await c.env.KV_CACHE.put(`ref_code:${uid}`, code);
    }

    return c.json({
      code,
      deeplink: `earnzy://ref/${code}`,
      shareUrl: `https://earnzy.com/ref/${code}`,
    });
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

// Get referral stats
app.get('/stats', async (c) => {
  try {
    const token = c.req.header('Authorization')?.replace('Bearer ', '');
    if (!token) return c.json({ error: 'Unauthorized' }, 401);

    const uid = Buffer.from(token, 'base64').toString().split(':')[0];
    const adminUrl = c.env.ADMIN_API_URL;

    const response = await fetch(`${adminUrl}/api/referrals?userId=${uid}`);
    const referrals = await response.json();

    const stats = {
      totalReferrals: referrals.length,
      earnedCoins: referrals.reduce((sum: number, r: any) => sum + (r.bonusCoins || 50), 0),
      activeReferrals: referrals.filter((r: any) => !r.isInactive).length,
      referrals: referrals.slice(0, 10),
    };

    return c.json(stats);
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

// Accept referral
app.post('/accept', async (c) => {
  try {
    const body = await c.req.json();
    const referralCode = body.code;

    // Find referrer
    const referrers = await c.env.KV_CACHE.list({ prefix: 'ref_code:' });
    let referrerId = null;

    for (const item of referrers.keys) {
      const code = await c.env.KV_CACHE.get(item.name);
      if (code === referralCode) {
        referrerId = item.name.replace('ref_code:', '');
        break;
      }
    }

    if (!referrerId) {
      return c.json({ error: 'Invalid referral code' }, 400);
    }

    // Record referral
    const token = c.req.header('Authorization')?.replace('Bearer ', '');
    const uid = token ? Buffer.from(token.replace('Bearer ', ''), 'base64').toString().split(':')[0] : body.uid;

    await c.env.KV_CACHE.put(`referral:${uid}:${referrerId}`, 'true', {
      expirationTtl: 365 * 24 * 60 * 60, // 1 year
    });

    return c.json({ success: true, bonusCoins: 50 });
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

export default app;
