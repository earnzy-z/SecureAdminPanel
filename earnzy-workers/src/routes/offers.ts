import { Hono } from 'hono';

const app = new Hono();

// Get all offers with filtering
app.get('/', async (c) => {
  try {
    const adminUrl = c.env.ADMIN_API_URL;
    const category = c.req.query('category');
    const response = await fetch(`${adminUrl}/api/offers`);
    let offers = await response.json();

    // Filter by category if provided
    if (category) {
      offers = offers.filter((o: any) => o.category === category && o.isActive);
    } else {
      offers = offers.filter((o: any) => o.isActive);
    }

    return c.json({ offers });
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

// Get offer wall (organized by category)
app.get('/wall/list', async (c) => {
  try {
    const adminUrl = c.env.ADMIN_API_URL;
    const response = await fetch(`${adminUrl}/api/offers`);
    const offers = await response.json();

    const wall = offers
      .filter((o: any) => o.isActive)
      .reduce((acc: any, offer: any) => {
        if (!acc[offer.category]) acc[offer.category] = [];
        acc[offer.category].push(offer);
        return acc;
      }, {});

    return c.json({ wall });
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

// Claim offer reward
app.post('/:id/claim', async (c) => {
  try {
    const token = c.req.header('Authorization')?.replace('Bearer ', '');
    if (!token) return c.json({ error: 'Unauthorized' }, 401);

    const uid = Buffer.from(token, 'base64').toString().split(':')[0];
    const offerId = c.req.param('id');

    // Check if already claimed
    const claimKey = `offer_claim:${uid}:${offerId}`;
    const claimed = await c.env.KV_CACHE.get(claimKey);

    if (claimed) {
      return c.json({ error: 'Offer already claimed' }, 400);
    }

    // Record claim
    await c.env.KV_CACHE.put(claimKey, 'true', {
      expirationTtl: 30 * 24 * 60 * 60, // 30 days
    });

    return c.json({ success: true, message: 'Offer claimed! Check your wallet.' });
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

export default app;
