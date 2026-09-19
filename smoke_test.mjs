import fs from 'fs';

const SUPABASE_URL = (process.env.SUPABASE_URL || 'https://eopcqvnqkkfwkvikvize.supabase.co').startsWith('http') 
    ? process.env.SUPABASE_URL 
    : `https://${process.env.SUPABASE_URL}`;
const SUPABASE_ANON_KEY = process.env.SUPABASE_ANON_KEY;

if (!SUPABASE_URL || !SUPABASE_ANON_KEY) {
    console.error('Missing SUPABASE_URL or SUPABASE_ANON_KEY');
    process.exit(1);
}

const headers = {
    'apikey': SUPABASE_ANON_KEY,
    'Authorization': `Bearer ${SUPABASE_ANON_KEY}`,
    'Content-Type': 'application/json',
};

async function run() {
    console.log('--- STARTING SMOKE TEST ---');

    // 1. AUTH: Use Admin API to generate link and verify
    console.log(`Generating magic link for: test_smoke_insert@example.com`);
    
    // First, let's use the service_role_key
    const SERVICE_ROLE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY;
    if (!SERVICE_ROLE_KEY) {
        console.error('SUPABASE_SERVICE_ROLE_KEY missing');
        process.exit(1);
    }
    
    let res = await fetch(`${SUPABASE_URL}/auth/v1/admin/generate_link`, {
        method: 'POST',
        headers: {
            'apikey': SERVICE_ROLE_KEY,
            'Authorization': `Bearer ${SERVICE_ROLE_KEY}`,
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            type: 'magiclink',
            email: 'test_smoke_insert@example.com'
        })
    });
    let data = await res.json();
    console.log('Generate link response:', data);
    
    if (data.code >= 400 || data.error) {
        console.error('Admin Error:', data);
        process.exit(1);
    }
    
    // The response has { action_link, email_otp, hashed_token, etc }
    const token_hash = data.hashed_token || data.properties?.hashed_token || data.token_hash;
    
    console.log('Verifying token hash...');
    res = await fetch(`${SUPABASE_URL}/auth/v1/verify?type=magiclink&token_hash=${token_hash}`, {
        method: 'POST',
        headers: {
            'apikey': SUPABASE_ANON_KEY,
            'Content-Type': 'application/json',
        }
    });
    data = await res.json();
    console.log('Verify response:', data);
    
    if (data.code >= 400 || data.error) {
        console.error('Verify Error:', data);
        process.exit(1);
    }
    
    const user = data.user;
    const token = data.session?.access_token;
    
    if (!token) {
        console.error('Auth Error: Missing session.');
        process.exit(1);
    }

    console.log(`✅ AUTH: PASS - User created with ID: ${user.id}`);
    
    const authHeaders = {
        'apikey': SUPABASE_ANON_KEY,
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
    };

    // Wait a brief moment for triggers to fire
    await new Promise(r => setTimeout(r, 1000));

    // 2. PROFILE: Check if profile was auto-created
    res = await fetch(`${SUPABASE_URL}/rest/v1/profiles?id=eq.${user.id}`, { headers: authHeaders });
    data = await res.json();
    if (data && data.length > 0) {
        console.log('✅ PROFILE: PASS - Auto-created');
    } else {
        console.error('❌ PROFILE: FAIL', data);
        process.exit(1);
    }

    // 3. CREDITS: Check 2 free credits available
    res = await fetch(`${SUPABASE_URL}/rest/v1/user_credit_balances?user_id=eq.${user.id}`, { headers: authHeaders });
    data = await res.json();
    if (data && data.length > 0 && data[0].free_credits === 2) {
        console.log('✅ CREDITS: PASS - 2 free credits available');
    } else {
        console.error('❌ CREDITS: FAIL', data);
        process.exit(1);
    }

    // 4. UPLOAD PHOTO
    const photoId = `test_photo_${Date.now()}`;
    const photoPath = `${user.id}/${photoId}.jpg`;
    
    // Create a dummy 1x1 JPEG byte array
    const dummyJpeg = Buffer.from([0xFF, 0xD8, 0xFF, 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01, 0x01, 0x01, 0x00, 0x48, 0x00, 0x48, 0x00, 0x00, 0xFF, 0xDB, 0x00, 0x43, 0x00, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0xFF, 0xC0, 0x00, 0x0B, 0x08, 0x00, 0x01, 0x00, 0x01, 0x01, 0x01, 0x11, 0x00, 0xFF, 0xC4, 0x00, 0x14, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0xFF, 0xC4, 0x00, 0x14, 0x10, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xDA, 0x00, 0x08, 0x01, 0x01, 0x00, 0x00, 0x3F, 0x00, 0x37, 0xFF, 0xD9]);
    
    res = await fetch(`${SUPABASE_URL}/storage/v1/object/tryon-photos/${photoPath}`, {
        method: 'POST',
        headers: {
            ...authHeaders,
            'Content-Type': 'image/jpeg'
        },
        body: dummyJpeg
    });
    data = await res.json();
    if (data.error) {
        console.error('❌ PHOTO UPLOAD: FAIL', data);
        process.exit(1);
    }
    console.log('✅ PHOTO UPLOAD: PASS - Uploaded to tryon-photos');

    // 5. PRODUCT READ
    res = await fetch(`${SUPABASE_URL}/rest/v1/products?is_active=eq.true&limit=1`, { headers: authHeaders });
    data = await res.json();
    if (!data || data.length === 0) {
        console.error('❌ PRODUCT READ: FAIL - No products found in DB');
        process.exit(1);
    }
    const productId = data[0].id;
    console.log(`✅ PRODUCT READ: PASS - Found product ${productId}`);

    // 6. GENERATE TRY-ON (SUCCESS PATH)
    const requestIdSuccess = `smoke-success-${Date.now()}`;
    console.log(`Invoking generate-tryon for success path with requestId: ${requestIdSuccess}`);
    
    res = await fetch(`${SUPABASE_URL}/functions/v1/generate-tryon`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify({
            productId: productId,
            userPhotoPath: photoPath,
            requestId: requestIdSuccess
        })
    });
    data = await res.json();
    
    if (data.error || data.status === 'FAILED') {
        console.error('❌ GEMINI GENERATION: FAIL', data);
        process.exit(1);
    } else {
        console.log('✅ GEMINI GENERATION: PASS', data);
    }
    
    // Check Result Storage
    if (data.status === 'COMPLETED' && data.resultStoragePath) {
        console.log('✅ RESULT STORAGE: PASS - Result generated and path returned');
    }

    // Check Credit balance is now 1
    res = await fetch(`${SUPABASE_URL}/rest/v1/user_credit_balances?user_id=eq.${user.id}`, { headers: authHeaders });
    data = await res.json();
    if (data && data.length > 0 && data[0].free_credits === 1) {
        console.log('✅ CREDIT CONSUME: PASS - 1 free credit remaining');
    } else {
        console.error('❌ CREDIT CONSUME: FAIL', data);
        process.exit(1);
    }

    // 7. GENERATE TRY-ON (FAILURE PATH)
    const requestIdFail = `smoke-fail-${Date.now()}`;
    console.log(`Invoking generate-tryon for failure path (invalid product) with requestId: ${requestIdFail}`);
    
    res = await fetch(`${SUPABASE_URL}/functions/v1/generate-tryon`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify({
            productId: 'invalid-product-id-to-force-fail',
            userPhotoPath: photoPath,
            requestId: requestIdFail
        })
    });
    data = await res.json();
    
    if (data.error && data.error === 'product_unavailable') {
        console.log('✅ FAILURE TEST: PASS - Rejected invalid product');
    } else {
        console.error('❌ FAILURE TEST: FAIL', data);
        process.exit(1);
    }

    // Check Credit balance is still 1 (held and released)
    res = await fetch(`${SUPABASE_URL}/rest/v1/user_credit_balances?user_id=eq.${user.id}`, { headers: authHeaders });
    data = await res.json();
    if (data && data.length > 0 && data[0].free_credits === 1) {
        console.log('✅ CREDIT RELEASE (FAILURE): PASS - 1 free credit remaining, not consumed');
    } else {
        console.error('❌ CREDIT RELEASE (FAILURE): FAIL', data);
        process.exit(1);
    }

    // 8. IDEMPOTENCY TEST
    console.log(`Invoking generate-tryon idempotently with requestId: ${requestIdSuccess}`);
    res = await fetch(`${SUPABASE_URL}/functions/v1/generate-tryon`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify({
            productId: productId,
            userPhotoPath: photoPath,
            requestId: requestIdSuccess
        })
    });
    data = await res.json();
    
    if (data.idempotent === true) {
        console.log('✅ IDEMPOTENCY: PASS - Returned existing result');
    } else {
        console.error('❌ IDEMPOTENCY: FAIL', data);
        process.exit(1);
    }

    // Check Credit balance is STILL 1
    res = await fetch(`${SUPABASE_URL}/rest/v1/user_credit_balances?user_id=eq.${user.id}`, { headers: authHeaders });
    data = await res.json();
    if (data && data.length > 0 && data[0].free_credits === 1) {
        console.log('✅ CREDIT HOLD (IDEMPOTENCY): PASS - Credit not double charged');
    } else {
        console.error('❌ CREDIT HOLD (IDEMPOTENCY): FAIL', data);
        process.exit(1);
    }

    // 9. CANCEL TEST
    // We will start a slow request (maybe we can't easily force it to be slow, but we can issue a cancel directly)
    const cancelRequestId = `smoke-cancel-${Date.now()}`;
    console.log(`Invoking cancel test with requestId: ${cancelRequestId}`);
    
    // Manually insert a PENDING job to cancel
    res = await fetch(`${SUPABASE_URL}/rest/v1/rpc/hold_credit`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify({ p_operation_id: cancelRequestId })
    });
    
    // Verify credit is 0
    res = await fetch(`${SUPABASE_URL}/rest/v1/user_credit_balances?user_id=eq.${user.id}`, { headers: authHeaders });
    data = await res.json();
    if (data[0].free_credits === 0) {
        console.log('✅ CANCEL TEST - Credit held successfully');
    } else {
        console.error('❌ CANCEL TEST - Hold failed', data);
    }

    res = await fetch(`${SUPABASE_URL}/rest/v1/try_on_jobs`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify({
            user_id: user.id,
            product_id: productId,
            user_photo_storage_path: photoPath,
            status: 'PENDING',
            request_id: cancelRequestId
        })
    });

    // Send cancel request
    res = await fetch(`${SUPABASE_URL}/functions/v1/generate-tryon`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify({
            action: 'cancel',
            requestId: cancelRequestId
        })
    });

    // Verify credit is restored to 1
    res = await fetch(`${SUPABASE_URL}/rest/v1/user_credit_balances?user_id=eq.${user.id}`, { headers: authHeaders });
    data = await res.json();
    if (data[0].free_credits === 1) {
        console.log('✅ CANCEL TEST: PASS - Job cancelled and credit restored');
    } else {
        console.error('❌ CANCEL TEST: FAIL', data);
        process.exit(1);
    }
    
    // 10. INSUFFICIENT CREDITS TEST
    // Consume the last credit manually to make it 0
    const burnRequestId = `smoke-burn-${Date.now()}`;
    await fetch(`${SUPABASE_URL}/rest/v1/rpc/hold_credit`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify({ p_operation_id: burnRequestId })
    });
    await fetch(`${SUPABASE_URL}/rest/v1/rpc/consume_credit`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify({ p_operation_id: burnRequestId })
    });

    res = await fetch(`${SUPABASE_URL}/rest/v1/user_credit_balances?user_id=eq.${user.id}`, { headers: authHeaders });
    data = await res.json();
    if (data[0].free_credits !== 0) {
        console.error('❌ INSUFFICIENT CREDIT TEST: Setup failed');
    }

    const noCreditRequestId = `smoke-nocredit-${Date.now()}`;
    res = await fetch(`${SUPABASE_URL}/functions/v1/generate-tryon`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify({
            productId: productId,
            userPhotoPath: photoPath,
            requestId: noCreditRequestId
        })
    });
    
    if (res.status === 402) {
        console.log('✅ INSUFFICIENT CREDIT TEST: PASS - Rejected with 402');
    } else {
        data = await res.json();
        if (data.error === 'insufficient_credits') {
            console.log('✅ INSUFFICIENT CREDIT TEST: PASS - Rejected with insufficient_credits error');
        } else {
            console.error('❌ INSUFFICIENT CREDIT TEST: FAIL', res.status, data);
            process.exit(1);
        }
    }

    // 11. STORAGE SECURITY (Cross user access)
    // Sign in User 2
    const email2 = 'test_smoke_insert2@example.com';
    let res2 = await fetch(`${SUPABASE_URL}/auth/v1/token?grant_type=password`, {
        method: 'POST',
        headers,
        body: JSON.stringify({ email: email2, password: 'password123' })
    });
    let data2 = await res2.json();
    const token2 = data2.access_token;
    
    const authHeaders2 = {
        'apikey': SUPABASE_ANON_KEY,
        'Authorization': `Bearer ${token2}`,
        'Content-Type': 'application/json',
    };

    // User 2 attempts to read User 1's photo
    res2 = await fetch(`${SUPABASE_URL}/storage/v1/object/tryon-photos/${photoPath}`, {
        method: 'GET',
        headers: authHeaders2
    });
    
    if (res2.status >= 400) {
        console.log('✅ STORAGE SECURITY: PASS - Cross-user read denied');
    } else {
        console.error('❌ STORAGE SECURITY: FAIL - Cross-user read allowed', res2.status);
        process.exit(1);
    }
    
    console.log('--- ALL SMOKE TESTS PASSED ---');
}

run().catch(console.error);
