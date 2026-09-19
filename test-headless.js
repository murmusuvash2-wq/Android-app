const puppeteer = require('puppeteer');
(async () => {
    const browser = await puppeteer.launch({ args: ['--no-sandbox'] });
    const page = await browser.newPage();
    
    page.on('console', msg => console.log('PAGE LOG:', msg.text()));
    page.on('pageerror', err => console.log('PAGE ERROR:', err.toString()));
    
    await page.goto('http://localhost:3000/');
    
    const bodyHTML = await page.evaluate(() => document.body.innerHTML);
    const isConfigVisible = await page.evaluate(() => {
        const el = document.getElementById('config-modal');
        return el ? !el.classList.contains('hidden') : false;
    });
    console.log('Is Config Modal Visible?', isConfigVisible);
    
    await browser.close();
})();
