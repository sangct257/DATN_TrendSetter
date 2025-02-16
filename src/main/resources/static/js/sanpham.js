let currentPage = 0; // Mặc định là trang 0 để đồng bộ với API
const pageSize = 8; // Số sản phẩm mỗi trang

async function fetchProducts(page = 0) {
    try {
        const response = await fetch(`http://localhost:8080/api/san-pham/hien-thi?page=${page}&size=${pageSize}`);
        const data = await response.json(); // Giả sử API trả về dữ liệu dạng { content: [...], totalPages: X }

        const productListContainer = document.getElementById('product-list');
        productListContainer.innerHTML = ''; // Xóa các sản phẩm cũ

        // Lặp qua từng sản phẩm và tạo phần tử HTML để hiển thị
        data.content.forEach(product => {
            const productElement = document.createElement('div');
            productElement.classList.add('col-lg-4', 'col-md-6', 'col-sm-12', 'pb-1');
            productElement.innerHTML = `
                <div class="card product-item border-0 mb-4">
                    <div class="card-header product-img position-relative overflow-hidden bg-transparent border p-0">
                        <img class="img-fluid w-100" src="${product.hinhAnhChinhUrl || 'img/default-product.jpg'}" alt="${product.tenSanPham}">
                    </div>
                    <div class="card-body border-left border-right text-center p-0 pt-4 pb-3">
                        <h6 class="text-truncate mb-3">${product.tenSanPham}</h6>
                        <div class="d-flex justify-content-center">
                            <h6>$${product.gia.toFixed(2)}</h6>
                        </div>
                    </div>
                    <div class="card-footer d-flex justify-content-between bg-light border">
                        <a href="#" class="btn btn-sm text-dark p-0"><i class="fas fa-eye text-primary mr-1"></i>Xem nhanh</a>
                        <a href="#" class="btn btn-sm text-dark p-0"><i class="fas fa-shopping-cart text-primary mr-1"></i>Thêm vào giỏ hàng</a>
                    </div>
                </div>
            `;
            productListContainer.appendChild(productElement);
        });

        // Cập nhật các nút phân trang
        updatePaginationControls(data.totalPages);
    } catch (error) {
        console.error('Error fetching products:', error);
    }
}

function updatePaginationControls(totalPages) {
    const paginationContainer = document.getElementById('pagination-controls');
    paginationContainer.innerHTML = ''; // Xóa các nút phân trang cũ

    // Nút Previous
    const previousButton = document.createElement('li');
    previousButton.className = `page-item ${currentPage === 0 ? 'disabled' : ''}`;
    previousButton.innerHTML = `
        <a class="page-link" href="#" aria-label="Previous" onclick="changePage(${currentPage - 1})">
            <span aria-hidden="true">&laquo;</span>
        </a>
    `;
    paginationContainer.appendChild(previousButton);

    // Các nút số trang
    for (let i = 0; i < totalPages; i++) {
        const pageButton = document.createElement('li');
        pageButton.className = `page-item ${currentPage === i ? 'active' : ''}`;
        pageButton.innerHTML = `<a class="page-link" href="#" onclick="changePage(${i})">${i + 1}</a>`;
        paginationContainer.appendChild(pageButton);
    }

    // Nút Next
    const nextButton = document.createElement('li');
    nextButton.className = `page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}`;
    nextButton.innerHTML = `
        <a class="page-link" href="#" aria-label="Next" onclick="changePage(${currentPage + 1})">
            <span aria-hidden="true">&raquo;</span>
        </a>
    `;
    paginationContainer.appendChild(nextButton);
}

function changePage(page) {
    if (page < 0 || page === currentPage) return;
    currentPage = page;
    fetchProducts(page);
}

// Gọi hàm fetchProducts khi trang được tải
window.onload = () => fetchProducts(currentPage);
