const { createClient } = require('@supabase/supabase-js');

const supabaseUrl = process.env.SUPABASE_URL.startsWith('http') ? process.env.SUPABASE_URL : 'https://' + process.env.SUPABASE_URL;
const supabaseKey = process.env.SUPABASE_ANON_KEY;

const supabase = createClient(supabaseUrl, supabaseKey);

async function run() {
    const email = 'nonadmin_test_' + Date.now() + '@example.com';
    const password = 'Password123!';
    
    console.log("Creating temporary non-admin user:", email);
    const { data: authData, error: authErr } = await supabase.auth.signUp({ email, password });
    if (authErr) {
        console.error("Sign up error:", authErr);
        return;
    }
    
    if (!authData.session) {
        console.error("No session returned. Email confirmation might be required.");
        return;
    }
    
    console.log("Logged in as non-admin user. JWT acquired.");
    
    console.log("Invoking admin-publish-product edge function...");
    const { data, error } = await supabase.functions.invoke('admin-publish-product', {
        body: { draft_id: 'fake-draft-123', updated_data: {} }
    });
    
    console.log("\n--- RESPONSE ---");
    if (error) {
        console.log("HTTP Error:", error.name, error.message);
        console.log("Context:", JSON.stringify(error.context, null, 2));
    } else {
        console.log("Data:", JSON.stringify(data, null, 2));
    }
}

run();
