const SUPABASE_URL = process.env.SUPABASE_URL.startsWith('http') ? process.env.SUPABASE_URL : 'https://' + process.env.SUPABASE_URL;
const SERVICE_ROLE = process.env.SUPABASE_SERVICE_ROLE_KEY || "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVvcGNxdm5xa2tmd2t2aWt2aXplIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc4OTQ2Mjk2NiwiZXhwIjoyMTA1MDM4OTY2fQ.GZBPBUL29Bj3pnHv7yFUWWFD4QnQQ4zowS20iZ_rDFk";
const ANON_KEY = process.env.SUPABASE_ANON_KEY;

async function run() {
    console.log("1. Admin Login (suvash.astrology@gmail.com)...");
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

    console.log("2. Fetching Draft ID for 'Zara | Floral Summer Dress'...");
    let draftsRes = await fetch(`${SUPABASE_URL}/rest/v1/product_drafts?status=eq.draft&select=*`, {
        method: 'GET',
        headers: { 'apikey': ANON_KEY, 'Authorization': `Bearer ${jwt}` }
    });
    let draftsData = await draftsRes.json();
    
    const draft = draftsData.find(d => d.extracted_data.name === 'Floral Summer Dress');
    if (!draft) {
        console.error("Could not find the draft!");
        return;
    }
    console.log(`Found draft ID: ${draft.id} - Current Status: ${draft.status}`);

    // Restore the valid image URL
    const updatedData = {
        ...draft.extracted_data,
        product_images: ["https://example.com/zara-dress.jpg"]
    };

    console.log("\n3. Invoking admin-publish-product with valid URL...");
    let efRes = await fetch(`${SUPABASE_URL}/functions/v1/admin-publish-product`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${jwt}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ draft_id: draft.id, updated_data: updatedData })
    });
    
    console.log(`HTTP Status: ${efRes.status} ${efRes.statusText}`);
    const text = await efRes.text();
    let publishedProductId = null;
    try {
        const json = JSON.parse(text);
        console.log("Response Body:\n" + JSON.stringify(json, null, 2));
        publishedProductId = json.product_id;
    } catch(e) {
        console.log("Response Body:\n" + text);
    }

    console.log("\n4. Verifying database state...");
    // Check if draft is now 'published'
    let checkDraftRes = await fetch(`${SUPABASE_URL}/rest/v1/product_drafts?id=eq.${draft.id}`, {
        method: 'GET',
        headers: { 'apikey': ANON_KEY, 'Authorization': `Bearer ${jwt}` }
    });
    let checkDraftData = await checkDraftRes.json();
    console.log(`Draft Status after publish: ${checkDraftData[0]?.status}`);

    console.log("\n5. Checking Pending Drafts query (should not return published drafts)...");
    let checkPendingRes = await fetch(`${SUPABASE_URL}/rest/v1/product_drafts?status=eq.draft`, {
        method: 'GET',
        headers: { 'apikey': ANON_KEY, 'Authorization': `Bearer ${jwt}` }
    });
    let checkPendingData = await checkPendingRes.json();
    const isStillPending = checkPendingData.some(d => d.id === draft.id);
    console.log(`Is the published draft still in the pending query? ${isStillPending ? 'YES (FAIL)' : 'NO (PASS)'}`);

    console.log("\n6. Checking public.products Android query (is_active=true)...");
    let checkProdRes = await fetch(`${SUPABASE_URL}/rest/v1/products?is_active=eq.true&name=eq.Floral%20Summer%20Dress`, {
        method: 'GET',
        headers: { 'apikey': ANON_KEY, 'Authorization': `Bearer ${jwt}` }
    });
    let checkProdData = await checkProdRes.json();
    console.log(`Matching products in public.products: ${checkProdData.length}`);
    
    console.log("\n7. Final Android Payload Verification:");
    if (checkProdData.length > 0) {
        console.log(JSON.stringify(checkProdData[0], null, 2));
    }
}
run();
