const SUPABASE_URL = process.env.SUPABASE_URL.startsWith('http') ? process.env.SUPABASE_URL : 'https://' + process.env.SUPABASE_URL;
const ANON_KEY = process.env.SUPABASE_ANON_KEY;

async function run() {
    let prodRes = await fetch(`${SUPABASE_URL}/rest/v1/products?is_active=eq.true`, {
        method: 'GET',
        headers: { 'apikey': ANON_KEY, 'Authorization': `Bearer ${ANON_KEY}` }
    });
    let prodData = await prodRes.json();
    console.log(`Android App received ${prodData.length} active products.`);
    prodData.forEach(p => {
        console.log(`- ${p.brand} | ${p.name}`);
    });
}
run();
