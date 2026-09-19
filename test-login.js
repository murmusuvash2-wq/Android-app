const puppeteer = require('puppeteer');
(async () => {
    const browser = await puppeteer.launch({ args: ['--no-sandbox'] });
    const page = await browser.newPage();
    
    page.on('console', msg => console.log('PAGE LOG:', msg.text()));
    page.on('pageerror', err => console.log('PAGE ERROR:', err.toString()));
    
    await page.goto('http://localhost:3000/');
    
    await page.evaluate(async () => {
        // mock valid supabase client
        window.supabase = {
            createClient: () => ({
                auth: {
                    getSession: async () => ({ data: { session: null } }),
                    onAuthStateChange: () => {},
                    signInWithPassword: async () => ({ error: null }),
                    signOut: async () => {}
                },
                from: () => ({
                    select: () => ({
                        eq: () => ({
                            order: async () => ({ data: [], error: null })
                        })
                    }),
                    insert: async () => ({ error: null })
                }),
                functions: {
                    invoke: async () => ({ data: {}, error: null })
                }
            })
        };
        
        const urlInput = document.getElementById('config-url');
        const keyInput = document.getElementById('config-key');
        if(urlInput && keyInput) {
            urlInput.value = 'http://test';
            keyInput.value = 'testkey';
            document.getElementById('config-form').dispatchEvent(new Event('submit'));
        }
        
        document.getElementById('email-input').value = 'admin@test.com';
        document.getElementById('password-input').value = 'password';
        document.getElementById('login-form').dispatchEvent(new Event('submit'));
    });
    
    // Wait a bit to see if anything crashes
    await new Promise(r => setTimeout(r, 1000));
    
    await browser.close();
})();
