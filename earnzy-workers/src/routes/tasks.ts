import { Hono } from 'hono';

const app = new Hono();

// Get all tasks
app.get('/', async (c) => {
  try {
    const adminUrl = c.env.ADMIN_API_URL;
    const response = await fetch(`${adminUrl}/api/tasks`);
    const tasks = await response.json();

    return c.json({
      tasks: tasks.filter((t: any) => t.isActive),
      meta: { total: tasks.length },
    });
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

// Get task details
app.get('/:id', async (c) => {
  try {
    const adminUrl = c.env.ADMIN_API_URL;
    const response = await fetch(`${adminUrl}/api/tasks/${c.req.param('id')}`);
    const task = await response.json();

    return c.json(task);
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

// Complete task
app.post('/:id/complete', async (c) => {
  try {
    const token = c.req.header('Authorization')?.replace('Bearer ', '');
    if (!token) return c.json({ error: 'Unauthorized' }, 401);

    const uid = Buffer.from(token, 'base64').toString().split(':')[0];
    const taskId = c.req.param('id');

    // Record completion
    const body = await c.req.json();
    const result = {
      userId: uid,
      taskId,
      completedAt: new Date().toISOString(),
      reward: body.reward || 10,
    };

    // Store in KV with TTL (prevent duplicate claims)
    const key = `task_complete:${uid}:${taskId}`;
    const existing = await c.env.KV_CACHE.get(key);
    
    if (existing) {
      return c.json({ error: 'Task already completed' }, 400);
    }

    await c.env.KV_CACHE.put(key, JSON.stringify(result), {
      expirationTtl: 24 * 60 * 60, // 24 hours
    });

    return c.json({ success: true, reward: result.reward });
  } catch (error: any) {
    return c.json({ error: error.message }, 500);
  }
});

export default app;
