import { Hono } from 'hono';

const app = new Hono();

// Get available rewards/redemptions
app.get('/', async (c) => {
  try {
    const rewards = [
      {
        id: 'paytm',
        name: 'Paytm Wallet',
        icon: 'paytm',
        minCoins: 100,
        rewards: [
          { amount: 100, coins: 100 },
          { amount: 500, coins: 500 },
          { amount: 1000, coins: 1000 },
        ],
      },
      {
        id: 'upi',
        name: 'UPI Transfer',
        icon: 'bank',
        minCoins: 100,
        rewards: [
          { amount: 100, coins: 100 },
          { amount: 500, coins: 500 },
          { amount: 1000, coins: 1000 },
        ],
      },
      {
        id: 'gift_card',
        name: 'Gift Cards',
        icon: 'gift',
        minCoins: 500,
        rewards: [
          { name: 'Amazon ₹500', coins: 500 },
          { name: 'Flipkart ₹1000', coins: 1000 },
          { name: 'Google Play ₹500', coins: 500 },
        ],
      },
    ];

    return c.json({ rewards });
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

// Request redemption
app.post('/request', async (c) => {
  try {
    const token = c.req.header('Authorization')?.replace('Bearer ', '');
    if (!token) return c.json({ error: 'Unauthorized' }, 401);

    const uid = Buffer.from(token, 'base64').toString().split(':')[0];
    const body = await c.req.json();
    const { rewardId, amount, upiId } = body;

    // Create redemption request
    const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const request = {
      id: requestId,
      userId: uid,
      rewardId,
      amount,
      upiId,
      status: 'pending',
      requestedAt: new Date().toISOString(),
    };

    // Store in KV (will be processed by admin)
    await c.env.KV_CACHE.put(`redemption:${requestId}`, JSON.stringify(request), {
      expirationTtl: 30 * 24 * 60 * 60, // 30 days
    });

    return c.json({
      success: true,
      requestId,
      message: 'Redemption request submitted. You will receive your reward within 24 hours.',
    });
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

// Get redemption history
app.get('/history', async (c) => {
  try {
    const token = c.req.header('Authorization')?.replace('Bearer ', '');
    if (!token) return c.json({ error: 'Unauthorized' }, 401);

    const uid = Buffer.from(token, 'base64').toString().split(':')[0];

    // List all redemptions for user
    const redemptions = await c.env.KV_CACHE.list({ prefix: `redemption:` });
    const userRedemptions = [];

    for (const item of redemptions.keys) {
      const data = await c.env.KV_CACHE.get(item.name);
      if (data) {
        const redemption = JSON.parse(data);
        if (redemption.userId === uid) {
          userRedemptions.push(redemption);
        }
      }
    }

    return c.json({
      redemptions: userRedemptions.sort((a, b) => 
        new Date(b.requestedAt).getTime() - new Date(a.requestedAt).getTime()
      ),
    });
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

export default app;
