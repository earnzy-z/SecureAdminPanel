import { Hono } from 'hono';

const app = new Hono();

// Get user balance
app.get('/balance', async (c) => {
  try {
    const token = c.req.header('Authorization')?.replace('Bearer ', '');
    if (!token) return c.json({ error: 'Unauthorized' }, 401);

    const uid = Buffer.from(token, 'base64').toString().split(':')[0];
    const adminUrl = c.env.ADMIN_API_URL;

    const response = await fetch(`${adminUrl}/api/users/${uid}`);
    const user = await response.json();

    return c.json({
      coins: user.coins || 0,
      level: Math.floor((user.coins || 0) / 1000) + 1,
      nextLevelCoins: (Math.floor((user.coins || 0) / 1000) + 1) * 1000,
    });
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

// Get transaction history
app.get('/history', async (c) => {
  try {
    const token = c.req.header('Authorization')?.replace('Bearer ', '');
    if (!token) return c.json({ error: 'Unauthorized' }, 401);

    const uid = Buffer.from(token, 'base64').toString().split(':')[0];
    const adminUrl = c.env.ADMIN_API_URL;

    const response = await fetch(`${adminUrl}/api/coins/history/${uid}`);
    const history = await response.json();

    return c.json({
      transactions: history.slice(0, 50), // Last 50 transactions
      total: history.length,
    });
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

export default app;
