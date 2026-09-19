// admin-panel/app.js

let supabaseClient = null;
let currentDraftId = null;
let currentSourceUrl = null;
let currentDraftExtractedData = {};

// In-memory cache of loaded drafts for fast instant filtering & counts
let allDrafts = [];
let activeFilter = 'all'; // 'all' | 'ready' | 'needs_review'
let searchQuery = '';

// DOM Elements
const configModal = document.getElementById('config-modal');
const configForm = document.getElementById('config-form');
const authSection = document.getElementById('auth-section');
const loginForm = document.getElementById('login-form');
const workspaceSection = document.getElementById('workspace-section');
const userEmailSpan = document.getElementById('user-email');
const logoutBtn = document.getElementById('logout-btn');

// CSV Elements
const csvForm = document.getElementById('csv-form');
const csvInput = document.getElementById('csv-input');
const dropZone = document.getElementById('drop-zone');
const fileNameDisplay = document.getElementById('file-name-display');
const csvBtn = document.getElementById('csv-btn');
const csvBtnText = document.getElementById('csv-btn-text');
const csvSpinner = document.getElementById('csv-spinner');
const csvMessage = document.getElementById('csv-message');

// KPIs & Filter Elements
const kpiTotalDrafts = document.getElementById('kpi-total-drafts');
const kpiReadyDrafts = document.getElementById('kpi-ready-drafts');
const kpiNeedsDrafts = document.getElementById('kpi-needs-drafts');
const filterCountAll = document.getElementById('filter-count-all');
const filterCountReady = document.getElementById('filter-count-ready');
const filterCountNeeds = document.getElementById('filter-count-needs');
const searchInput = document.getElementById('search-input');
const filterPills = document.querySelectorAll('.filter-pill');

// Drafts List & Refresh
const draftsList = document.getElementById('drafts-list');
const refreshDraftsBtn = document.getElementById('refresh-drafts-btn');

// Review Section Elements
const reviewSection = document.getElementById('review-section');
const reviewStatusBadge = document.getElementById('review-status-badge');
const imagePreviewsContainer = document.getElementById('image-previews-container');
const publishForm = document.getElementById('publish-form');
const publishBtn = document.getElementById('publish-btn');
const publishBtnText = document.getElementById('publish-btn-text');
const publishSpinner = document.getElementById('publish-spinner');
const publishMessage = document.getElementById('publish-message');

// -------------------------------------------------------------
// Initialization
// -------------------------------------------------------------
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

function showAuth() {
    if (authSection) authSection.classList.remove('hidden');
    if (workspaceSection) workspaceSection.classList.add('hidden');
    if (userEmailSpan) userEmailSpan.textContent = '';
    if (logoutBtn) logoutBtn.classList.add('hidden');
}

async function showWorkspace(user) {
    try {
        const authSec = document.getElementById('auth-section');
        const workspaceSec = document.getElementById('workspace-section');
        const emailSpan = document.getElementById('user-email');
        const logout = document.getElementById('logout-btn');
        
        if (authSec) authSec.classList.add('hidden');
        if (emailSpan && user) emailSpan.textContent = user.email || 'Admin';
        if (logout) logout.classList.remove('hidden');
        
        // --- ADMIN AUTHORIZATION QUERY (Strict RBAC against public.admins) ---
        try {
            const { data: adminData, error: adminErr } = await supabaseClient
                .from('admins')
                .select('user_id')
                .eq('user_id', user.id)
                .single();
                
            if (adminErr || !adminData) {
                console.warn("Admin authorization failed:", adminErr);
                if (workspaceSec) {
                    workspaceSec.innerHTML = `<div class="p-6 bg-red-50 text-red-700 rounded-2xl border border-red-200 shadow-subtle">
                        <h3 class="font-bold text-base mb-1">Access Denied: Administrator Privileges Required</h3>
                        <p class="text-xs mb-3 text-red-600">Your account is not registered in the TiHin admin registry.</p>
                        <span class="text-[11px] font-mono opacity-80 block bg-white p-3 rounded-lg border border-red-100">
                        UID: ${user.id}<br>
                        Error: ${adminErr ? adminErr.message + ' (' + adminErr.code + ')' : 'No matching row found in public.admins.'}
                        </span>
                    </div>`;
                    workspaceSec.classList.remove('hidden');
                }
                return;
            }
        } catch (err) {
            console.error("Crash during admin check:", err);
            if (workspaceSec) {
                workspaceSec.innerHTML = '<div class="p-6 bg-red-50 text-red-700 rounded-2xl border border-red-200">System Error during authorization check.</div>';
                workspaceSec.classList.remove('hidden');
            }
            return;
        }
        
        if (workspaceSec) workspaceSec.classList.remove('hidden');
        
        // Load pending drafts
        loadDrafts().catch(err => console.error("loadDrafts unhandled:", err));
    } catch(err) {
        console.error("Critical error in showWorkspace:", err);
        alert("UI Error: " + err.message);
    }
}

// -------------------------------------------------------------
// Authentication Event Handlers
// -------------------------------------------------------------
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

// -------------------------------------------------------------
// CSV Drag & Drop and File Selection
// -------------------------------------------------------------
if (dropZone && csvInput) {
    dropZone.addEventListener('click', () => {
        csvInput.click();
    });

    csvInput.addEventListener('change', () => {
        if (csvInput.files && csvInput.files[0]) {
            fileNameDisplay.textContent = `Selected: ${csvInput.files[0].name} (${(csvInput.files[0].size / 1024).toFixed(1)} KB)`;
            fileNameDisplay.classList.remove('text-slate');
            fileNameDisplay.classList.add('text-forest', 'font-semibold');
        } else {
            fileNameDisplay.textContent = "No file chosen";
            fileNameDisplay.classList.add('text-slate');
            fileNameDisplay.classList.remove('text-forest', 'font-semibold');
        }
    });

    dropZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropZone.classList.add('border-forest', 'bg-forest/5');
    });

    dropZone.addEventListener('dragleave', () => {
        dropZone.classList.remove('border-forest', 'bg-forest/5');
    });

    dropZone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropZone.classList.remove('border-forest', 'bg-forest/5');
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            csvInput.files = e.dataTransfer.files;
            fileNameDisplay.textContent = `Selected: ${e.dataTransfer.files[0].name} (${(e.dataTransfer.files[0].size / 1024).toFixed(1)} KB)`;
            fileNameDisplay.classList.remove('text-slate');
            fileNameDisplay.classList.add('text-forest', 'font-semibold');
        }
    });
}

// -------------------------------------------------------------
// CSV Flow (14-Column Support, Exact Preserved Ingestion Logic)
// -------------------------------------------------------------
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

        csvMessage.textContent = `✓ Successfully imported ${drafts.length} product drafts into staging.`;
        csvMessage.className = 'text-xs font-semibold py-2 px-3 rounded-lg bg-green-50 text-green-700 border border-green-200';
        csvMessage.classList.remove('hidden');
        csvForm.reset();
        fileNameDisplay.textContent = "No file chosen";
        fileNameDisplay.className = "text-xs font-mono text-slate mt-1.5";
        
        loadDrafts();

    } catch (err) {
        csvMessage.textContent = err.message || "Failed to parse CSV or save drafts.";
        csvMessage.className = 'text-xs font-semibold py-2 px-3 rounded-lg bg-red-50 text-red-600 border border-red-200';
        csvMessage.classList.remove('hidden');
    } finally {
        setCsvLoading(false);
    }
});

function setCsvLoading(isLoading) {
    if (isLoading) {
        csvInput.disabled = true;
        csvBtn.disabled = true;
        csvBtnText.textContent = "Processing & Ingesting...";
        csvSpinner.classList.remove('hidden');
    } else {
        csvInput.disabled = false;
        csvBtn.disabled = false;
        csvBtnText.textContent = "Process & Ingest CSV";
        csvSpinner.classList.add('hidden');
    }
}

// -------------------------------------------------------------
// Classification Helper: Ready to Publish vs Needs Review
// -------------------------------------------------------------
function isDraftReadyToPublish(extracted) {
    const hasBrand = Boolean(extracted.brand && extracted.brand.trim() !== '');
    const hasPrice = extracted.price != null && !isNaN(extracted.price) && extracted.price >= 0;
    return hasBrand && hasPrice;
}

// -------------------------------------------------------------
// Load Drafts & KPI Updates
// -------------------------------------------------------------
async function loadDrafts() {
    if (!draftsList) return;
    
    try {
        draftsList.innerHTML = '<div class="py-8 text-center"><div class="w-6 h-6 border-2 border-forest border-t-transparent rounded-full animate-spin mx-auto mb-2"></div><p class="text-slate text-xs italic">Fetching pending drafts from catalog pipeline...</p></div>';
        const { data, error } = await supabaseClient
            .from('product_drafts')
            .select('*')
            .eq('status', 'draft')
            .order('created_at', { ascending: false });

        if (error) {
            draftsList.innerHTML = `<div class="p-4 bg-red-50 text-red-600 rounded-xl text-xs border border-red-200">Error loading drafts: ${error.message}</div>`;
            return;
        }

        allDrafts = data || [];
        updateKpisAndCounters();
        renderDraftsList();

    } catch(err) {
        console.error("Exception in loadDrafts:", err);
        draftsList.innerHTML = `<div class="p-4 bg-red-50 text-red-600 rounded-xl text-xs border border-red-200">Crash loading drafts: ${err.message}</div>`;
    }
}

if (refreshDraftsBtn) refreshDraftsBtn.addEventListener('click', loadDrafts);

function updateKpisAndCounters() {
    let readyCount = 0;
    let needsCount = 0;

    allDrafts.forEach(draft => {
        const extracted = draft.extracted_data || {};
        if (isDraftReadyToPublish(extracted)) {
            readyCount++;
        } else {
            needsCount++;
        }
    });

    const totalCount = allDrafts.length;

    if (kpiTotalDrafts) kpiTotalDrafts.textContent = totalCount;
    if (kpiReadyDrafts) kpiReadyDrafts.textContent = readyCount;
    if (kpiNeedsDrafts) kpiNeedsDrafts.textContent = needsCount;

    if (filterCountAll) filterCountAll.textContent = totalCount;
    if (filterCountReady) filterCountReady.textContent = readyCount;
    if (filterCountNeeds) filterCountNeeds.textContent = needsCount;
}

// -------------------------------------------------------------
// Filter and Search Toolbar Listeners
// -------------------------------------------------------------
if (searchInput) {
    searchInput.addEventListener('input', (e) => {
        searchQuery = e.target.value.trim().toLowerCase();
        renderDraftsList();
    });
}

filterPills.forEach(pill => {
    pill.addEventListener('click', () => {
        filterPills.forEach(p => {
            p.classList.remove('bg-charcoal', 'text-white', 'border-charcoal');
            p.classList.add('bg-canvas', 'text-charcoal', 'border-sandstone');
        });
        pill.classList.remove('bg-canvas', 'text-charcoal', 'border-sandstone');
        pill.classList.add('bg-charcoal', 'text-white', 'border-charcoal');

        activeFilter = pill.getAttribute('data-filter');
        renderDraftsList();
    });
});

// -------------------------------------------------------------
// Render Drafts Queue (Responsive Cards with Visual Thumbnails)
// -------------------------------------------------------------
function renderDraftsList() {
    if (!draftsList) return;

    if (allDrafts.length === 0) {
        draftsList.innerHTML = `
            <div class="py-12 px-4 text-center rounded-2xl bg-canvas border border-dashed border-sandstone">
                <div class="w-12 h-12 rounded-full bg-white border border-sandstone flex items-center justify-center mx-auto mb-2 text-slate">
                    <svg class="w-6 h-6 text-slate/80" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M5 13l4 4L19 7"></path></svg>
                </div>
                <h4 class="text-sm font-semibold text-charcoal">No pending drafts</h4>
                <p class="text-xs text-slate mt-1 max-w-sm mx-auto">Upload a catalog CSV above to stage new items for review.</p>
            </div>
        `;
        return;
    }

    // Filter by category pill
    let filtered = allDrafts.filter(draft => {
        const extracted = draft.extracted_data || {};
        const isReady = isDraftReadyToPublish(extracted);
        if (activeFilter === 'ready') return isReady;
        if (activeFilter === 'needs_review') return !isReady;
        return true;
    });

    // Filter by search query
    if (searchQuery) {
        filtered = filtered.filter(draft => {
            const extracted = draft.extracted_data || {};
            const title = (extracted.name || '').toLowerCase();
            const brand = (extracted.brand || '').toLowerCase();
            const cat = (extracted.category || '').toLowerCase();
            const site = (extracted.site || '').toLowerCase();
            return title.includes(searchQuery) || brand.includes(searchQuery) || cat.includes(searchQuery) || site.includes(searchQuery);
        });
    }

    if (filtered.length === 0) {
        draftsList.innerHTML = `
            <div class="py-10 px-4 text-center rounded-xl bg-canvas border border-sandstone">
                <p class="text-xs font-semibold text-charcoal">No products match your search</p>
                <p class="text-[11px] text-slate mt-0.5">Try clearing your search query or switching the status filter.</p>
            </div>
        `;
        return;
    }

    draftsList.innerHTML = '';

    filtered.forEach(draft => {
        const extracted = draft.extracted_data || {};
        const title = extracted.name || 'Untitled Product';
        const brand = extracted.brand || 'Unspecified Brand';
        const site = extracted.site || 'Direct Source';
        const category = extracted.category || extracted.gender || 'Fashion';
        const price = (extracted.price != null && !isNaN(extracted.price)) ? `₹${parseFloat(extracted.price).toLocaleString('en-IN')}` : 'Price Missing';
        const isReady = isDraftReadyToPublish(extracted);

        // Thumbnail image
        const images = Array.isArray(extracted.product_images) ? extracted.product_images : (extracted.image_url ? [extracted.image_url] : []);
        const thumbUrl = images.length > 0 ? images[0] : null;

        const thumbHtml = thumbUrl 
            ? `<img src="${thumbUrl}" alt="" class="w-14 h-16 sm:w-16 sm:h-20 object-cover rounded-xl border border-sandstone bg-canvas flex-shrink-0" onerror="this.onerror=null; this.src='data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 40 40%22><rect width=%2240%22 height=%2240%22 fill=%22%23F3F1ED%22/></svg>';">`
            : `<div class="w-14 h-16 sm:w-16 sm:h-20 rounded-xl bg-ecru border border-sandstone flex items-center justify-center text-[10px] text-slate font-medium flex-shrink-0">No Img</div>`;

        const badgeHtml = isReady
            ? '<span class="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-forest/10 text-forest border border-forest/20">Ready to Publish</span>'
            : '<span class="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-amber-50 text-amber-700 border border-amber-200">Needs Review</span>';

        const card = document.createElement('div');
        card.className = 'flex flex-col sm:flex-row sm:items-center justify-between p-3.5 sm:p-4 bg-canvas hover:bg-white border border-sandstone rounded-2xl gap-3 transition-all hover:shadow-subtle';
        
        card.innerHTML = `
            <div class="flex items-center gap-3.5 min-w-0">
                ${thumbHtml}
                <div class="min-w-0 flex-1">
                    <div class="flex items-center gap-2 flex-wrap mb-1">
                        <span class="text-[11px] font-bold uppercase tracking-wider text-charcoal/70">${brand}</span>
                        ${badgeHtml}
                        <span class="text-[10px] font-medium text-slate bg-ecru px-1.5 py-0.5 rounded">${site}</span>
                    </div>
                    <h4 class="text-sm font-bold text-charcoal truncate" title="${title}">${title}</h4>
                    <div class="flex items-center gap-3 text-xs text-slate mt-1">
                        <span class="font-semibold text-charcoal">${price}</span>
                        <span>•</span>
                        <span class="truncate">${category}</span>
                    </div>
                </div>
            </div>
            <div class="flex sm:flex-col items-center sm:items-end justify-between sm:justify-center pt-2 sm:pt-0 border-t sm:border-t-0 border-sandstone/60">
                <button type="button" class="review-draft-btn h-9 px-4 bg-white hover:bg-ecru text-charcoal border border-sandstone font-medium text-xs rounded-xl shadow-subtle hover:shadow transition flex items-center gap-1.5 self-end">
                    Review Details →
                </button>
            </div>
        `;

        card.querySelector('.review-draft-btn').addEventListener('click', () => populateReviewForm(draft));
        draftsList.appendChild(card);
    });
}

// -------------------------------------------------------------
// Populate Review Form
// -------------------------------------------------------------
function populateReviewForm(draft) {
    const extracted = draft.extracted_data || {};
    currentDraftExtractedData = extracted;
    currentDraftId = draft.id;
    currentSourceUrl = draft.source_url;

    // Status Badge & ID
    const isReady = isDraftReadyToPublish(extracted);
    if (reviewStatusBadge) {
        if (isReady) {
            reviewStatusBadge.textContent = "Ready to Publish";
            reviewStatusBadge.className = "px-2.5 py-1 rounded-full text-xs font-semibold bg-forest/10 text-forest border border-forest/20";
        } else {
            reviewStatusBadge.textContent = "Needs Review";
            reviewStatusBadge.className = "px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200";
        }
    }

    document.getElementById('draft-id-display').textContent = `Draft: ${draft.id.substring(0, 8)}`;

    // Image Previews Strip
    const rawImages = extracted.product_images || (extracted.image_url ? [extracted.image_url] : []);
    const images = Array.isArray(rawImages) ? rawImages.filter(Boolean) : [rawImages];
    
    if (imagePreviewsContainer) {
        imagePreviewsContainer.innerHTML = '';
        if (images.length === 0) {
            imagePreviewsContainer.innerHTML = '<div class="w-20 h-24 rounded-xl bg-ecru border border-sandstone flex items-center justify-center text-slate text-xs italic">No Images</div>';
        } else {
            images.forEach((imgUrl, index) => {
                const imgWrap = document.createElement('div');
                imgWrap.className = 'relative flex-shrink-0';
                imgWrap.innerHTML = `
                    <img src="${imgUrl}" class="w-20 h-24 object-cover rounded-xl border border-sandstone bg-canvas" alt="Preview ${index + 1}" onerror="this.onerror=null; this.src='data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 40 40%22><rect width=%2240%22 height=%2240%22 fill=%22%23F3F1ED%22/></svg>';">
                    <span class="absolute bottom-1 right-1 bg-charcoal/80 text-white text-[9px] px-1 py-0.2 rounded font-mono">${index + 1}</span>
                `;
                imagePreviewsContainer.appendChild(imgWrap);
            });
        }
    }

    // Section 1: Basic Information
    document.getElementById('draft-name').value = extracted.name || '';
    document.getElementById('draft-brand').value = extracted.brand || '';
    document.getElementById('draft-site').value = extracted.site || '';
    document.getElementById('draft-description').value = extracted.description || '';
    document.getElementById('draft-material').value = extracted.material || '';

    // Section 2: Pricing & Ratings
    document.getElementById('draft-price').value = (extracted.price != null && !isNaN(extracted.price)) ? extracted.price : '';
    document.getElementById('draft-original-price').value = (extracted.original_price != null && !isNaN(extracted.original_price)) ? extracted.original_price : (extracted.MRP || extracted.mrp || '');
    document.getElementById('draft-discount').value = (extracted.discount_percent != null && !isNaN(extracted.discount_percent)) ? extracted.discount_percent : (extracted['discount%'] || '');
    document.getElementById('draft-rating').value = (extracted.rating != null && !isNaN(extracted.rating)) ? extracted.rating : '';
    document.getElementById('draft-review-count').value = (extracted.review_count != null && !isNaN(extracted.review_count)) ? extracted.review_count : (extracted.rating_count || '');
    document.getElementById('draft-external-id').value = extracted.external_product_id || extracted.product_id || '';

    // Section 3: Metadata & Variants
    document.getElementById('draft-gender').value = extracted.gender || '';
    document.getElementById('draft-category').value = extracted.category || '';
    document.getElementById('draft-colors').value = Array.isArray(extracted.colors) ? extracted.colors.join(', ') : (extracted.color || '');
    document.getElementById('draft-sizes').value = Array.isArray(extracted.sizes) ? extracted.sizes.join(', ') : '';

    // Section 4: Images & Source Links
    document.getElementById('draft-product-url').value = extracted.product_url || extracted.merchant_url || draft.source_url || '';
    document.getElementById('draft-images').value = images.join('\n');

    reviewSection.classList.remove('hidden');
    reviewSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// -------------------------------------------------------------
// Discard / Cancel Review
// -------------------------------------------------------------
document.getElementById('cancel-btn').addEventListener('click', () => {
    reviewSection.classList.add('hidden');
    currentDraftId = null;
    currentSourceUrl = null;
    currentDraftExtractedData = {};
    if (publishMessage) {
        publishMessage.textContent = '';
        publishMessage.className = 'text-xs font-medium';
    }
});

// -------------------------------------------------------------
// Publish Flow (Calls admin-publish-product Edge Function)
// -------------------------------------------------------------
publishForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!currentDraftId) return;

    setPublishLoading(true);
    publishMessage.textContent = '';
    publishMessage.className = 'text-xs font-medium';

    try {
        const product_images = document.getElementById('draft-images').value.split('\n').map(s => s.trim()).filter(Boolean);
        const sizes = document.getElementById('draft-sizes').value.split(',').map(s => s.trim()).filter(Boolean);
        const colors = document.getElementById('draft-colors').value.split(',').map(s => s.trim()).filter(Boolean);

        const updatedData = {
            ...currentDraftExtractedData,
            name: document.getElementById('draft-name').value.trim(),
            brand: document.getElementById('draft-brand').value.trim(),
            site: document.getElementById('draft-site').value.trim() || null,
            description: document.getElementById('draft-description').value.trim() || null,
            material: document.getElementById('draft-material').value.trim() || null,
            price: parseFloat(document.getElementById('draft-price').value),
            original_price: document.getElementById('draft-original-price').value ? parseFloat(document.getElementById('draft-original-price').value) : null,
            discount_percent: document.getElementById('draft-discount').value ? parseFloat(document.getElementById('draft-discount').value) : null,
            rating: document.getElementById('draft-rating').value ? parseFloat(document.getElementById('draft-rating').value) : null,
            review_count: document.getElementById('draft-review-count').value ? parseInt(document.getElementById('draft-review-count').value, 10) : null,
            external_product_id: document.getElementById('draft-external-id').value.trim() || null,
            gender: document.getElementById('draft-gender').value.trim() || null,
            category: document.getElementById('draft-category').value.trim() || null,
            product_images,
            sizes: sizes.length > 0 ? sizes : null,
            colors: colors.length > 0 ? colors : null,
            merchant_url: document.getElementById('draft-product-url').value.trim() || currentSourceUrl
        };

        const { data, error } = await supabaseClient.functions.invoke('admin-publish-product', {
            body: { 
                draft_id: currentDraftId,
                updated_data: updatedData
            }
        });

        if (error) throw error;
        if (data.error) throw new Error(data.message || data.error);

        publishMessage.textContent = "✓ Product published successfully to TiHin catalog!";
        publishMessage.className = 'text-xs font-semibold py-2 px-3 rounded-lg bg-green-50 text-green-700 border border-green-200';
        
        loadDrafts(); // Refresh queue to remove published item
        
        setTimeout(() => {
            reviewSection.classList.add('hidden');
            publishMessage.textContent = '';
            currentDraftId = null;
        }, 2000);

    } catch (err) {
        publishMessage.textContent = err.message || "Failed to publish product.";
        publishMessage.className = 'text-xs font-semibold py-2 px-3 rounded-lg bg-red-50 text-red-600 border border-red-200';
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

// -------------------------------------------------------------
// Initialize on DOMContentLoaded
// -------------------------------------------------------------
document.addEventListener('DOMContentLoaded', init);
