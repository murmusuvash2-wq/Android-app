const SUPABASE_URL = process.env.SUPABASE_URL.startsWith('http') ? process.env.SUPABASE_URL : 'https://' + process.env.SUPABASE_URL;
const SERVICE_ROLE = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVvcGNxdm5xa2tmd2t2aWt2aXplIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc4OTQ2Mjk2NiwiZXhwIjoyMTA1MDM4OTY2fQ.GZBPBUL29Bj3pnHv7yFUWWFD4QnQQ4zowS20iZ_rDFk";

async function run() {
    let prodRes = await fetch(`${SUPABASE_URL}/rest/v1/products?name=in.(Floral%20Summer%20Dress,Casual%20Linen%20Shirt)`, {
        method: 'GET',
        headers: { 'apikey': SERVICE_ROLE, 'Authorization': `Bearer ${SERVICE_ROLE}` }
    });
    let prodData = await prodRes.json();
    console.log("Newly imported drafts in public.products:", prodData.length);
}
run();
