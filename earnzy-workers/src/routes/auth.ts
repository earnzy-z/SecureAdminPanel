import { Hono } from 'hono';
import { z } from 'zod';

const AuthSchema = z.object({
  uid: z.string().uuid(),
  email: z.string().email(),
  deviceToken: z.string().optional(),
});

const app = new Hono();

// Device registration / login
app.post('/register', async (c) => {
  try {
    const body = await c.req.json();
    const data = AuthSchema.parse(body);

    // Generate JWT token
    const token = await c.env.KV_CACHE.get(`token:${data.uid}`) || 
                  Buffer.from(`${data.uid}:${Date.now()}`).toString('base64');

    // Store device token for push notifications
    if (data.deviceToken) {
      await c.env.KV_CACHE.put(`device:${data.uid}`, data.deviceToken, {
        expirationTtl: 30 * 24 * 60 * 60, // 30 days
      });
    }

    return c.json({
      token,
      user: {
        uid: data.uid,
        email: data.email,
        coins: 0,
        level: 1,
      },
    });
  } catch (error: any) {
    return c.json({ error: error.message }, 400);
  }
});

// Get current user
app.get('/me', async (c) => {
  try {
    const token = c.req.header('Authorization')?.replace('Bearer ', '');
    if (!token) return c.json({ error: 'Unauthorized' }, 401);

    const [uid] = Buffer.from(token, 'base64').toString().split(':');
    
    // Fetch from admin API
    const response = await fetch(`${c.env.ADMIN_API_URL}/api/users/${uid}`);
    const user = await response.json();

    return c.json(user);
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

// Logout
app.post('/logout', async (c) => {
  try {
    const token = c.req.header('Authorization')?.replace('Bearer ', '');
    if (token) {
      const [uid] = Buffer.from(token, 'base64').toString().split(':');
      await c.env.KV_CACHE.delete(`token:${uid}`);
    }
    return c.json({ success: true });
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

export default app;
