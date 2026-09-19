const supabaseUrl = process.env.SUPABASE_URL.startsWith('http') ? process.env.SUPABASE_URL : 'https://' + process.env.SUPABASE_URL;
const supabaseKey = process.env.SUPABASE_ANON_KEY;

async function run() {
    const email = 'suvash.astrology+test' + Date.now() + '@gmail.com';
    const password = 'Password123!';
    
    console.log("Signing up:", email);
    let res = await fetch(`${supabaseUrl}/auth/v1/signup`, {
        method: 'POST',
        headers: {
            'apikey': supabaseKey,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email, password })
    });
    
    let authData = await res.json();
    if (!authData.session) {
        console.log("No session. It might require confirmation.");
        console.log(authData);
        return;
    }
    
    const jwt = authData.session.access_token;
    console.log("JWT acquired.");
    
    let fnRes = await fetch(`${supabaseUrl}/functions/v1/admin-publish-product`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${jwt}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ draft_id: 'fake-draft-123', updated_data: {} })
    });
    
    console.log(`\nHTTP Status: ${fnRes.status} ${fnRes.statusText}`);
    const text = await fnRes.text();
    try {
        console.log("Response JSON:\n", JSON.stringify(JSON.parse(text), null, 2));
    } catch(e) {
        console.log("Response Text:\n", text);
    }
}
run();
