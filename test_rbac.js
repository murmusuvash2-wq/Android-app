const SUPABASE_URL = process.env.SUPABASE_URL.startsWith('http') ? process.env.SUPABASE_URL : 'https://' + process.env.SUPABASE_URL;
const SERVICE_ROLE = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVvcGNxdm5xa2tmd2t2aWt2aXplIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc4OTQ2Mjk2NiwiZXhwIjoyMTA1MDM4OTY2fQ.GZBPBUL29Bj3pnHv7yFUWWFD4QnQQ4zowS20iZ_rDFk";
const ANON_KEY = process.env.SUPABASE_ANON_KEY;

async function run() {
    // 1. Generate link using Service Role
    let glRes = await fetch(`${SUPABASE_URL}/auth/v1/admin/generate_link`, {
        method: 'POST',
        headers: {
            'apikey': SERVICE_ROLE,
            'Authorization': `Bearer ${SERVICE_ROLE}`,
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ type: 'magiclink', email: 'murmu@gmail.com' })
    });
    let glData = await glRes.json();
    
    // 2. Verify link to get JWT session
    let verRes = await fetch(`${SUPABASE_URL}/auth/v1/verify?type=magiclink&token_hash=${glData.hashed_token}`, {
        method: 'POST',
        headers: { 'apikey': ANON_KEY, 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: 'magiclink', token_hash: glData.hashed_token })
    });
    let verData = await verRes.json();
    
    const jwt = verData.access_token;
    
    // 3. Call Edge Function with the non-admin JWT
    console.log("Invoking admin-publish-product with non-admin JWT...");
    let efRes = await fetch(`${SUPABASE_URL}/functions/v1/admin-publish-product`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${jwt}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ draft_id: 'fake-draft-123', updated_data: {} })
    });
    
    console.log(`\nHTTP Status: ${efRes.status} ${efRes.statusText}`);
    const text = await efRes.text();
    try {
        console.log("Response Body:\n" + JSON.stringify(JSON.parse(text), null, 2));
    } catch(e) {
        console.log("Response Body:\n" + text);
    }
}
run();
