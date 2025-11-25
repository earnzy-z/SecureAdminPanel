import { Hono } from 'hono';

const app = new Hono();

// Get all active promo codes
app.get('/', async (c) => {
  try {
    const adminUrl = c.env.ADMIN_API_URL;
    const response = await fetch(`${adminUrl}/api/promo-codes`);
    let promoCodes = await response.json();

    // Filter only active and valid codes
    const now = new Date();
    promoCodes = promoCodes.filter((p: any) => 
      p.isActive && 
      (!p.expiresAt || new Date(p.expiresAt) > now)
    );

    return c.json({ promoCodes });
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

// Redeem promo code
app.post('/redeem', async (c) => {
  try {
    const token = c.req.header('Authorization')?.replace('Bearer ', '');
    if (!token) return c.json({ error: 'Unauthorized' }, 401);

    const uid = Buffer.from(token, 'base64').toString().split(':')[0];
    const { code } = await c.req.json();

    // Check if already used
    const usedKey = `promo_used:${uid}:${code}`;
    const used = await c.env.KV_CACHE.get(usedKey);

    if (used) {
      return c.json({ error: 'Promo code already used' }, 400);
    }

    // Get promo details
    const adminUrl = c.env.ADMIN_API_URL;
    const response = await fetch(`${adminUrl}/api/promo-codes`);
    const promoCodes = await response.json();
    const promo = promoCodes.find((p: any) => p.code === code && p.isActive);

    if (!promo) {
      return c.json({ error: 'Invalid or expired promo code' }, 400);
    }

    // Record usage
    await c.env.KV_CACHE.put(usedKey, 'true', {
      expirationTtl: 365 * 24 * 60 * 60,
    });

    return c.json({
      success: true,
      reward: promo.reward,
      message: `You earned ${promo.reward} coins!`,
    });
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

export default app;
