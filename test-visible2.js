const puppeteer = require('puppeteer');
(async () => {
    const browser = await puppeteer.launch({ args: ['--no-sandbox'] });
    const page = await browser.newPage();
    
    page.on('console', msg => console.log('PAGE LOG:', msg.text()));
    
    await page.goto('http://localhost:3000/');
    
    await page.evaluate(async () => {
        window.supabase = {
            createClient: () => ({
                auth: {
                    getSession: async () => ({ data: { session: null } }),
                    onAuthStateChange: (cb) => { window.mockAuthCallback = cb; },
                    signInWithPassword: async () => {
                        window.mockAuthCallback('SIGNED_IN', { user: { email: 'test@example.com' } });
                        return { error: null };
                    },
                    signOut: async () => {}
                },
                from: () => ({
                    select: () => ({
                        eq: () => ({
                            order: async () => ({ data: [], error: null })
                        })
                    })
                })
            })
        };
        
        document.getElementById('config-url').value = 'http://test';
        document.getElementById('config-key').value = 'testkey';
        document.getElementById('config-form').dispatchEvent(new Event('submit'));
        
        document.getElementById('email-input').value = 'admin@test.com';
        document.getElementById('password-input').value = 'password';
        document.getElementById('login-form').dispatchEvent(new Event('submit', { cancelable: true }));
    });
    
    await new Promise(r => setTimeout(r, 1000));
    
    const wsClasses = await page.evaluate(() => {
        return document.getElementById('workspace-section').className;
    });
    console.log('Workspace Classes:', wsClasses);
    
    await browser.close();
})();
