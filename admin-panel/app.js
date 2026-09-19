// admin-panel/app.js

let supabaseClient = null;
let currentDraftId = null;
let currentSourceUrl = null;
let currentDraftExtractedData = {};

// DOM Elements
const configModal = document.getElementById('config-modal');
const configForm = document.getElementById('config-form');
const authSection = document.getElementById('auth-section');
const loginForm = document.getElementById('login-form');
const workspaceSection = document.getElementById('workspace-section');
const userEmailSpan = document.getElementById('user-email');
const logoutBtn = document.getElementById('logout-btn');

const csvForm = document.getElementById('csv-form');
const csvInput = document.getElementById('csv-input');
const csvBtn = document.getElementById('csv-btn');
const csvBtnText = document.getElementById('csv-btn-text');
const csvSpinner = document.getElementById('csv-spinner');
const csvMessage = document.getElementById('csv-message');

const draftsList = document.getElementById('drafts-list');
const refreshDraftsBtn = document.getElementById('refresh-drafts-btn');

// ... Review Section UI remains similar ...

const reviewSection = document.getElementById('review-section');
const publishForm = document.getElementById('publish-form');
const publishBtn = document.getElementById('publish-btn');
const publishBtnText = document.getElementById('publish-btn-text');
const publishSpinner = document.getElementById('publish-spinner');
const publishMessage = document.getElementById('publish-message');

// Initialize
function init() {
    const envUrl = window.__ENV__?.SUPABASE_URL;
    const envKey = window.__ENV__?.SUPABASE_ANON_KEY;
    const savedUrl = localStorage.getItem('supabase_url') || (envUrl && envUrl.trim() !== '' ? envUrl : null);
    const savedKey = localStorage.getItem('supabase_key') || (envKey && envKey.trim() !== '' ? envKey : null);

    if (savedUrl && savedKey) {
        initSupabase(savedUrl, savedKey);
    } else {
        configModal.classList.remove('hidden');
        configModal.classList.add('flex');
    }
}

configForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const url = document.getElementById('config-url').value.trim();
    const key = document.getElementById('config-key').value.trim();
    localStorage.setItem('supabase_url', url);
    localStorage.setItem('supabase_key', key);
    configModal.classList.add('hidden');
    configModal.classList.remove('flex');
    initSupabase(url, key);
});

async function initSupabase(url, key) {
    try {
        supabaseClient = window.supabase.createClient(url, key);
        checkSession();
        setupAuthListeners();
    } catch (err) {
        console.error("Failed to initialize Supabase", err);
        localStorage.removeItem('supabase_url');
        localStorage.removeItem('supabase_key');
        init(); // Retry
    }
}

async function checkSession() {
    try {
        const { data, error } = await supabaseClient.auth.getSession();
        if (error) throw error;
        if (data?.session) {
            showWorkspace(data.session.user);
        } else {
            showAuth();
        }
    } catch (err) {
        console.error("Session check failed:", err);
        showAuth();
    }
}

function setupAuthListeners() {
    supabaseClient.auth.onAuthStateChange((event, session) => {
        if (event === 'SIGNED_IN' && session) {
            showWorkspace(session.user);
        } else if (event === 'SIGNED_OUT') {
            showAuth();
        }
    });
}

function showAuth() { console.log("showAuth called!", new Error().stack);
    authSection.classList.remove('hidden');
    workspaceSection.classList.add('hidden');
    userEmailSpan.textContent = '';
    logoutBtn.classList.add('hidden');
}

async function showWorkspace(user) {
    try {
        console.log("showWorkspace started for user", user?.email);
        const authSec = document.getElementById('auth-section');
        const workspaceSec = document.getElementById('workspace-section');
        const emailSpan = document.getElementById('user-email');
        const logout = document.getElementById('logout-btn');
        
        if (authSec) authSec.classList.add('hidden');
        if (emailSpan && user) emailSpan.textContent = user.email || 'Admin';
        if (logout) logout.classList.remove('hidden');
        
        // --- ADMIN AUTHORIZATION QUERY ---
        try {
            console.log("Authenticated as:", user.id, "Email:", user.email);
            console.log("Checking public.admins where user_id =", user.id);
            
            const { data: adminData, error: adminErr } = await supabaseClient
                .from('admins')
                .select('user_id')
                .eq('user_id', user.id)
                .single();
                
            console.log("Admin query response:", { adminData, adminErr });
                
            if (adminErr || !adminData) {
                console.warn("Admin authorization failed:", adminErr);
                if (workspaceSec) {
                    workspaceSec.innerHTML = `<div class="p-6 bg-red-50 text-red-700 rounded-xl border border-red-200">
                        <b>Access Denied: You do not have administrator privileges.</b><br><br>
                        <span class="text-sm font-mono opacity-80">
                        Authenticated UID: ${user.id}<br>
                        Query Error: ${adminErr ? adminErr.message + ' (' + adminErr.code + ')' : 'No matching row found in public.admins.'}
                        </span>
                    </div>`;
                    workspaceSec.classList.remove('hidden');
                }
                return; // Stop execution
            }
        } catch (err) {
            console.error("Crash during admin check:", err);
            if (workspaceSec) {
                workspaceSec.innerHTML = '<div class="p-6 bg-red-50 text-red-700 rounded-xl border border-red-200">System Error during authorization check.</div>';
                workspaceSec.classList.remove('hidden');
            }
            return;
        }
        
        if (workspaceSec) workspaceSec.classList.remove('hidden');
        
        // Wrap loadDrafts so it cannot crash this function
        loadDrafts().catch(err => console.error("loadDrafts unhandled:", err));
    } catch(err) {
        console.error("Critical error in showWorkspace:", err);
        alert("UI Error: " + err.message);
    }
}

// Authentication

loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('email-input').value;
    const password = document.getElementById('password-input').value;
    const errorEl = document.getElementById('auth-error');
    errorEl.classList.add('hidden');

    try {
        const { data, error } = await supabaseClient.auth.signInWithPassword({ email, password });
        if (error) {
            errorEl.textContent = error.message;
            errorEl.classList.remove('hidden');
        }
    } catch (err) {
        console.error("Login crash:", err);
        errorEl.textContent = err.message || "Failed to sign in.";
        errorEl.classList.remove('hidden');
    }
});

logoutBtn.addEventListener('click', async () => {
    await supabaseClient.auth.signOut();
});

// CSV Flow
csvForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const file = csvInput.files[0];
    if (!file) return;

    setCsvLoading(true);
    csvMessage.classList.add('hidden');
    reviewSection.classList.add('hidden');

    try {
        const text = await file.text();
        const rows = text.split('\n').map(row => row.trim()).filter(Boolean);
        if (rows.length < 2) throw new Error("CSV is empty or missing headers.");

        // Skip header row
        const headers = rows[0].split(',').map(h => h.trim().toLowerCase());
        
        const drafts = [];
        for (let i = 1; i < rows.length; i++) {
            // Simple split by comma (assuming no commas inside values for this basic MVP)
            const cols = rows[i].split(',');
            if (cols.length < headers.length) continue;

            const data = {};
            headers.forEach((h, idx) => {
                data[h] = cols[idx] ? cols[idx].trim() : null;
            });

            const extracted = {
                brand: data.brand || null,
                name: data.name || null,
                price: parseFloat(data.price) || null,
                original_price: data.original_price ? parseFloat(data.original_price) : (data.MRP ? parseFloat(data.MRP) : (data.mrp ? parseFloat(data.mrp) : null)),
                description: data.description || null,
                material: data.material || null,
                sizes: data.sizes ? data.sizes.split('|').map(s => s.trim()) : [],
                colors: data.colors ? data.colors.split('|').map(c => c.trim()) : (data.color ? data.color.split('|').map(c => c.trim()) : []),
                product_images: data.product_images ? data.product_images.split('|').map(i => i.trim()) : (data.image_url ? data.image_url.split('|').map(i => i.trim()) : []),
                product_url: data.product_url || data.merchant_url || null,
                site: data.site || null,
                external_product_id: data.product_id || data.external_product_id || null,
                gender: data.gender || null,
                category: data.category || null,
                discount_percent: data['discount%'] ? parseFloat(data['discount%']) : (data.discount_percent ? parseFloat(data.discount_percent) : null),
                rating: data.rating ? parseFloat(data.rating) : null,
                review_count: data.rating_count ? parseInt(data.rating_count, 10) : (data.review_count ? parseInt(data.review_count, 10) : null)
            };

            drafts.push({
                source_url: data.product_url || data.merchant_url || 'csv-import',
                status: 'draft',
                extracted_data: extracted
            });
        }

        if (drafts.length === 0) throw new Error("No valid rows found to import.");

        // Insert into Supabase (RLS allows if Admin)
        const { error } = await supabaseClient.from('product_drafts').insert(drafts);
        if (error) throw error;

        csvMessage.textContent = `Successfully imported ${drafts.length} drafts.`;
        csvMessage.className = 'text-sm mt-2 text-green-600';
        csvMessage.classList.remove('hidden');
        csvForm.reset();
        
        loadDrafts();

    } catch (err) {
        csvMessage.textContent = err.message || "Failed to parse CSV or save drafts.";
        csvMessage.className = 'text-sm mt-2 text-red-500';
        csvMessage.classList.remove('hidden');
    } finally {
        setCsvLoading(false);
    }
});

function setCsvLoading(isLoading) {
    if (isLoading) {
        csvInput.disabled = true;
        csvBtn.disabled = true;
        csvBtnText.textContent = "Processing...";
        csvSpinner.classList.remove('hidden');
    } else {
        csvInput.disabled = false;
        csvBtn.disabled = false;
        csvBtnText.textContent = "Process CSV";
        csvSpinner.classList.add('hidden');
    }
}

async function loadDrafts() {
    const list = document.getElementById('drafts-list');
    if (!list) return;
    
    try {
        list.innerHTML = '<p class="text-stone-500 text-sm italic">Loading...</p>';
        const { data, error } = await supabaseClient
            .from('product_drafts')
            .select('*')
            .eq('status', 'draft')
            .order('created_at', { ascending: false });

        if (error) {
            list.innerHTML = `<p class="text-red-500 text-sm">Error loading drafts: ${error.message}</p>`;
            return;
        }

        if (!data || data.length === 0) {
            list.innerHTML = '<p class="text-stone-500 text-sm italic">No pending drafts.</p>';
            return;
        }

        list.innerHTML = '';
        data.forEach(draft => {
            const extracted = draft.extracted_data || {};
            const title = extracted.name || 'Unknown Product';
            const brand = extracted.brand || 'Unknown Brand';
            
            // Classification: Ready to Publish requires brand and price >= 0
            const isReady = Boolean(extracted.brand && extracted.brand.trim() !== '' && extracted.price != null && !isNaN(extracted.price) && extracted.price >= 0);
            const badgeHtml = isReady
                ? '<span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800">Ready to Publish</span>'
                : '<span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-amber-100 text-amber-800">Needs Review</span>';
            
            const el = document.createElement('div');
            el.className = 'flex justify-between items-center p-3 border border-stone-200 rounded-lg hover:bg-stone-50 transition-colors';
            el.innerHTML = `
                <div>
                    <div class="flex items-center gap-2 mb-1">
                        <p class="font-medium text-sm text-stone-900">${title}</p>
                        ${badgeHtml}
                    </div>
                    <p class="text-xs text-stone-500">${brand} • ${draft.source_url}</p>
                </div>
                <button class="bg-white border border-stone-300 px-3 py-1 text-sm rounded hover:bg-stone-100 review-draft-btn">Review</button>
            `;
            
            el.querySelector('.review-draft-btn').addEventListener('click', () => populateReviewForm(draft));
            list.appendChild(el);
        });
    } catch(err) {
        console.error("Exception in loadDrafts:", err);
        list.innerHTML = `<p class="text-red-500 text-sm">Crash loading drafts: ${err.message}</p>`;
    }
}
const refreshBtn = document.getElementById('refresh-drafts-btn');
if (refreshBtn) refreshBtn.addEventListener('click', loadDrafts);
function populateReviewForm(draft) {
    const extracted = draft.extracted_data || {};
    currentDraftExtractedData = extracted;
    
    currentDraftId = draft.id;
    currentSourceUrl = draft.source_url;

    document.getElementById('draft-id-display').textContent = `Draft: ${draft.id.substring(0,8)}`;
    
    document.getElementById('draft-brand').value = extracted.brand || '';
    document.getElementById('draft-name').value = extracted.name || '';
    document.getElementById('draft-price').value = extracted.price || '';
    document.getElementById('draft-original-price').value = extracted.original_price || '';
    document.getElementById('draft-description').value = extracted.description || '';
    document.getElementById('draft-material').value = extracted.material || '';
    
    document.getElementById('draft-sizes').value = Array.isArray(extracted.sizes) ? extracted.sizes.join(', ') : '';
    document.getElementById('draft-colors').value = Array.isArray(extracted.colors) ? extracted.colors.join(', ') : '';
    
    document.getElementById('draft-images').value = Array.isArray(extracted.product_images) 
        ? extracted.product_images.join('\n') 
        : '';

    reviewSection.classList.remove('hidden');
    reviewSection.scrollIntoView({ behavior: 'smooth' });
}

// Cancel
document.getElementById('cancel-btn').addEventListener('click', () => {
    reviewSection.classList.add('hidden');
    csvInput.value = '';
    currentDraftId = null;
    currentSourceUrl = null;
    currentDraftExtractedData = {};
});

// Publish Flow
publishForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!currentDraftId) return;

    setPublishLoading(true);
    publishMessage.textContent = '';
    publishMessage.className = 'text-sm text-right mt-2 font-medium';

    try {
        // Collect updated data
        const product_images = document.getElementById('draft-images').value.split('\n').map(s => s.trim()).filter(Boolean);
        const sizes = document.getElementById('draft-sizes').value.split(',').map(s => s.trim()).filter(Boolean);
        const colors = document.getElementById('draft-colors').value.split(',').map(s => s.trim()).filter(Boolean);

        const updatedData = {
            ...currentDraftExtractedData,
            brand: document.getElementById('draft-brand').value.trim(),
            name: document.getElementById('draft-name').value.trim(),
            price: parseFloat(document.getElementById('draft-price').value),
            original_price: document.getElementById('draft-original-price').value ? parseFloat(document.getElementById('draft-original-price').value) : null,
            description: document.getElementById('draft-description').value.trim(),
            material: document.getElementById('draft-material').value.trim(),
            product_images,
            sizes: sizes.length > 0 ? sizes : null,
            colors: colors.length > 0 ? colors : null,
            merchant_url: currentSourceUrl
        };

        const { data, error } = await supabaseClient.functions.invoke('admin-publish-product', {
            body: { 
                draft_id: currentDraftId,
                updated_data: updatedData
            }
        });

        if (error) throw error;
        if (data.error) throw new Error(data.message || data.error);

        publishMessage.textContent = "Product published successfully!";
        publishMessage.classList.add('text-green-600');
        
        loadDrafts(); // Refresh list to remove published item
        
        setTimeout(() => {
            reviewSection.classList.add('hidden');
            csvInput.value = '';
            publishMessage.textContent = '';
        }, 2000);

    } catch (err) {
        publishMessage.textContent = err.message || "Failed to publish product.";
        publishMessage.classList.add('text-red-500');
    } finally {
        setPublishLoading(false);
    }
});

function setPublishLoading(isLoading) {
    if (isLoading) {
        publishBtn.disabled = true;
        publishBtnText.textContent = "Publishing...";
        publishSpinner.classList.remove('hidden');
    } else {
        publishBtn.disabled = false;
        publishBtnText.textContent = "Publish to Catalog";
        publishSpinner.classList.add('hidden');
    }
}

// Start
document.addEventListener('DOMContentLoaded', init);
