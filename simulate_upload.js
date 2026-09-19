const fs = require('fs');
const SUPABASE_URL = process.env.SUPABASE_URL.startsWith('http') ? process.env.SUPABASE_URL : 'https://' + process.env.SUPABASE_URL;
const SERVICE_ROLE = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVvcGNxdm5xa2tmd2t2aWt2aXplIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc4OTQ2Mjk2NiwiZXhwIjoyMTA1MDM4OTY2fQ.GZBPBUL29Bj3pnHv7yFUWWFD4QnQQ4zowS20iZ_rDFk";
const ANON_KEY = process.env.SUPABASE_ANON_KEY;

async function run() {
    console.log("1. Admin Login (getting JWT for suvash.astrology@gmail.com)...");
    let glRes = await fetch(`${SUPABASE_URL}/auth/v1/admin/generate_link`, {
        method: 'POST',
        headers: { 'apikey': SERVICE_ROLE, 'Authorization': `Bearer ${SERVICE_ROLE}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: 'magiclink', email: 'suvash.astrology@gmail.com' })
    });
    let glData = await glRes.json();
    
    let verRes = await fetch(`${SUPABASE_URL}/auth/v1/verify?type=magiclink&token_hash=${glData.hashed_token}`, {
        method: 'POST',
        headers: { 'apikey': ANON_KEY, 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: 'magiclink', token_hash: glData.hashed_token })
    });
    let verData = await verRes.json();
    const jwt = verData.access_token;
    console.log("Admin JWT acquired.");

    console.log("\n2. Uploading 2-row CSV data to product_drafts...");
    const drafts = [
        {
            source_url: "https://example.com/shop/zara-dress",
            status: "draft",
            extracted_data: {
                brand: "Zara",
                name: "Floral Summer Dress",
                price: 49.99,
                original_price: 79.99,
                description: null,
                material: null,
                sizes: [],
                colors: ["Red"],
                product_images: ["https://example.com/zara-dress.jpg"]
            }
        },
        {
            source_url: "https://example.com/shop/hm-shirt",
            status: "draft",
            extracted_data: {
                brand: "H&M",
                name: "Casual Linen Shirt",
                price: 29.99,
                original_price: 39.99,
                description: null,
                material: null,
                sizes: [],
                colors: ["White"],
                product_images: ["https://example.com/hm-shirt.jpg"]
            }
        }
    ];

    let insertRes = await fetch(`${SUPABASE_URL}/rest/v1/product_drafts`, {
        method: 'POST',
        headers: {
            'apikey': ANON_KEY,
            'Authorization': `Bearer ${jwt}`,
            'Content-Type': 'application/json',
            'Prefer': 'return=representation'
        },
        body: JSON.stringify(drafts)
    });
    let insertData = await insertRes.json();
    console.log(`Inserted ${insertData.length || 0} rows. Status: ${insertRes.status}`);

    console.log("\n3/4. Confirming rows appear in Pending Drafts...");
    let draftsRes = await fetch(`${SUPABASE_URL}/rest/v1/product_drafts?status=eq.draft&order=created_at.desc&limit=2`, {
        method: 'GET',
        headers: { 'apikey': ANON_KEY, 'Authorization': `Bearer ${jwt}` }
    });
    let draftsData = await draftsRes.json();
    console.log(`Found ${draftsData.length} pending drafts.`);
    
    console.log("\n5/6. Opening one draft in Review/Edit (checking extracted_data)...");
    if (draftsData.length > 0) {
        console.log(JSON.stringify(draftsData[0].extracted_data, null, 2));
    }

    console.log("\n7/8. Confirming unpublished drafts do NOT appear in Android (public.products)...");
    let prodRes = await fetch(`${SUPABASE_URL}/rest/v1/products?brand=in.(Zara,H%26M)`, {
        method: 'GET',
        headers: { 'apikey': ANON_KEY, 'Authorization': `Bearer ${jwt}` }
    });
    let prodData = await prodRes.json();
    console.log(`Found ${prodData.length} matching products in public.products (expected 0).`);
}
run();
