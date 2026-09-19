const fs = require('fs');
let js = fs.readFileSync('admin-panel/app.js', 'utf8');

const oldMapping = `            const extracted = {
                brand: data.brand || null,
                name: data.name || null,
                price: parseFloat(data.price) || null,
                original_price: data.original_price ? parseFloat(data.original_price) : null,
                description: data.description || null,
                material: data.material || null,
                sizes: data.sizes ? data.sizes.split('|').map(s => s.trim()) : [],
                colors: data.colors ? data.colors.split('|').map(c => c.trim()) : [],
                product_images: data.product_images ? data.product_images.split('|').map(i => i.trim()) : []
            };
            drafts.push({
                source_url: data.merchant_url || 'csv-import',
                status: 'draft',
                extracted_data: extracted
            });`;

const newMapping = `            const extracted = {
                brand: data.brand || null,
                name: data.name || null,
                price: parseFloat(data.price) || null,
                original_price: data.original_price ? parseFloat(data.original_price) : (data.mrp ? parseFloat(data.mrp) : null),
                description: data.description || null,
                material: data.material || null,
                sizes: data.sizes ? data.sizes.split('|').map(s => s.trim()) : [],
                colors: data.colors ? data.colors.split('|').map(c => c.trim()) : (data.color ? data.color.split('|').map(c => c.trim()) : []),
                product_images: data.product_images ? data.product_images.split('|').map(i => i.trim()) : (data.image_url ? data.image_url.split('|').map(i => i.trim()) : [])
            };
            drafts.push({
                source_url: data.merchant_url || data.product_url || 'csv-import',
                status: 'draft',
                extracted_data: extracted
            });`;

js = js.replace(oldMapping, newMapping);
fs.writeFileSync('admin-panel/app.js', js);
console.log("Updated CSV mapping in app.js");
