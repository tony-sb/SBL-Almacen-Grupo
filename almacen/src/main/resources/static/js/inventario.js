class InventoryManager {
    constructor() {
        this.baseUrl = '/api/inventario';
        this.inventoryData = [];
        this.grupos = [];
        this.init();
    }

    async init() {
        await this.loadGrupos();
        await this.loadInventory();
        this.setupEventListeners();
    }

    async loadGrupos() {
        try {
            const response = await fetch(`${this.baseUrl}/grupos`);
            const data = await response.json();

            if (data.success) {
                this.grupos = data.grupos;
                this.populateGroupFilter();
            }
        } catch (error) {
            console.error('Error al cargar grupos:', error);
            // Grupos por defecto si hay error
            this.grupos = ['20', '44', '65', '75', '85', '150', '270', '350'];
            this.populateGroupFilter();
        }
    }

    populateGroupFilter() {
        const groupFilter = document.getElementById('groupFilter');
        if (groupFilter) {
            groupFilter.innerHTML = '<option value="">Todos los grupos</option>';

            this.grupos.forEach(grupo => {
                const option = document.createElement('option');
                option.value = grupo;
                option.textContent = `Grupo ${grupo}`;
                groupFilter.appendChild(option);
            });
        }
    }

    async loadInventory() {
        try {
            this.showLoading();
            const response = await fetch(this.baseUrl);
            const data = await response.json();

            if (data.success) {
                this.inventoryData = data.productos;
                this.renderTable(this.inventoryData);
                this.updateAlertCount(data.productosStockBajo);
            } else {
                this.showError('Error al cargar el inventario');
            }
        } catch (error) {
            this.showError('Error de conexión: ' + error.message);
            this.loadSampleData();
        }
    }

    async descargarPDF() {
        try {
            // Mostrar loading en el botón
            const btn = event.target;
            const originalHtml = btn.innerHTML;
            btn.innerHTML = '<i class="bi bi-hourglass-split"></i> Generando PDF...';
            btn.disabled = true;

            // Usar la nueva ruta
            const response = await fetch('/descargar-inventario-completo');

            if (response.ok) {
                // Crear blob y descargar
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.style.display = 'none';
                a.href = url;

                // Obtener el nombre del archivo del header
                const contentDisposition = response.headers.get('Content-Disposition');
                let filename = 'inventario_completo.pdf';
                if (contentDisposition) {
                    const filenameMatch = contentDisposition.match(/filename="?(.+)"?/);
                    if (filenameMatch) {
                        filename = filenameMatch[1];
                    }
                }

                a.download = filename;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);

                this.showSuccess('PDF generado exitosamente');
            } else {
                throw new Error('Error al generar el PDF');
            }

        } catch (error) {
            console.error('Error al descargar PDF:', error);
            this.showError('Error al generar el PDF: ' + error.message);
        } finally {
            // Restaurar botón después de 1 segundo
            setTimeout(() => {
                const btn = document.querySelector('.btn-success');
                if (btn) {
                    btn.innerHTML = '<i class="bi bi-file-pdf"></i> Imprimir PDF';
                    btn.disabled = false;
                }
            }, 1000);
        }
    }
    renderTable(data) {
        const tableBody = document.getElementById('inventoryTableBody');
        tableBody.innerHTML = '';

        if (data.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-muted py-4">
                        <i class="bi bi-inbox display-6 d-block mb-2"></i>
                        No se encontraron productos
                    </td>
                </tr>
            `;
            return;
        }

        let lowStockCount = 0;

        data.forEach(product => {
            const row = document.createElement('tr');

            // Determinar estado del stock
            const stockStatus = this.getStockStatus(product);
            if (stockStatus === 'low') {
                row.classList.add('stock-bajo');
                lowStockCount++;
            } else if (stockStatus === 'normal') {
                row.classList.add('stock-normal');
            }

            // Formatear cantidad
            const cantidadDisplay = this.formatCantidad(product.cantidad);
            const estadoDisplay = this.getEstadoDisplay(stockStatus);

            row.innerHTML = `
                <td>${this.escapeHtml(product.nombre)}</td>
                <td>${product.codigo ? this.escapeHtml(product.codigo) : '<span class="text-muted">-</span>'}</td>
                <td>${cantidadDisplay}</td>
                <td>${product.categoria || '<span class="text-muted">-</span>'}</td>
                <td>${estadoDisplay}</td>
            `;

            tableBody.appendChild(row);
        });

        this.updateAlertCount(lowStockCount);
    }

    getStockStatus(product) {
        if (product.cantidad === null || product.cantidad === undefined) {
            return 'unknown';
        }

        const stockMinimo = product.stockMinimo || 5;
        if (product.cantidad <= stockMinimo) {
            return 'low';
        }

        return 'normal';
    }

    formatCantidad(cantidad) {
        if (cantidad === null || cantidad === undefined) {
            return '<span class="text-muted">-</span>';
        }

        if (typeof cantidad === 'number') {
            return cantidad;
        }

        if (typeof cantidad === 'string' && !isNaN(cantidad)) {
            return parseInt(cantidad);
        }

        return `<span class="text-muted">${this.escapeHtml(cantidad)}</span>`;
    }

    getEstadoDisplay(stockStatus) {
        switch (stockStatus) {
            case 'low':
                return `
                    <span class="badge bg-danger">
                        <span class="stock-indicator stock-bajo-indicator"></span>
                        Stock Bajo
                    </span>
                `;
            case 'normal':
                return `
                    <span class="badge bg-success">
                        <span class="stock-indicator stock-normal-indicator"></span>
                        Normal
                    </span>
                `;
            default:
                return `
                    <span class="badge bg-secondary">
                        <span class="stock-indicator stock-desconocido-indicator"></span>
                        Sin datos
                    </span>
                `;
        }
    }

    updateAlertCount(count) {
        const alertElement = document.getElementById('alertCount');
        if (alertElement) {
            alertElement.textContent = `${count} alertas`;
            alertElement.className = `badge bg-${count > 0 ? 'warning' : 'success'}`;
        }
    }

    filterProducts() {
        const searchTerm = document.getElementById('searchInput').value.toLowerCase();
        const groupFilter = document.getElementById('groupFilter').value;
        const stockFilter = document.getElementById('stockFilter').value;

        let filteredData = this.inventoryData;

        // Filtro por búsqueda
        if (searchTerm) {
            filteredData = filteredData.filter(product =>
                product.nombre.toLowerCase().includes(searchTerm) ||
                (product.codigo && product.codigo.toLowerCase().includes(searchTerm))
            );
        }

        // Filtro por grupo (usando categoria como grupo)
        if (groupFilter) {
            filteredData = filteredData.filter(product =>
                product.categoria && product.categoria.toString() === groupFilter
            );
        }

        // Filtro por stock
        if (stockFilter === 'low') {
            filteredData = filteredData.filter(product => this.getStockStatus(product) === 'low');
        } else if (stockFilter === 'normal') {
            filteredData = filteredData.filter(product => this.getStockStatus(product) === 'normal');
        }

        this.renderTable(filteredData);
    }

    setupEventListeners() {
        const searchInput = document.getElementById('searchInput');
        const groupFilter = document.getElementById('groupFilter');
        const stockFilter = document.getElementById('stockFilter');

        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                this.filterProducts();
            });
        }

        if (groupFilter) {
            groupFilter.addEventListener('change', () => {
                this.filterProducts();
            });
        }

        if (stockFilter) {
            stockFilter.addEventListener('change', () => {
                this.filterProducts();
            });
        }
    }

    showLoading() {
        const tableBody = document.getElementById('inventoryTableBody');
        if (tableBody) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center py-4">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">Cargando...</span>
                        </div>
                        <div class="mt-2">Cargando inventario...</div>
                    </td>
                </tr>
            `;
        }
    }

    showError(message) {
        const alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-danger alert-dismissible fade show';
        alertDiv.innerHTML = `
            <i class="bi bi-exclamation-triangle me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        const pageHeader = document.querySelector('.page-header');
        if (pageHeader) {
            pageHeader.parentNode.insertBefore(alertDiv, pageHeader.nextSibling);
        }

        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 5000);
    }

    showSuccess(message) {
        const alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-success alert-dismissible fade show';
        alertDiv.innerHTML = `
            <i class="bi bi-check-circle me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        const pageHeader = document.querySelector('.page-header');
        if (pageHeader) {
            pageHeader.parentNode.insertBefore(alertDiv, pageHeader.nextSibling);
        }

        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 3000);
    }

    loadSampleData() {
        this.inventoryData = [
            { id: 1, nombre: "Augmentin 625 Duo Tablet", codigo: "88/88/88", cantidad: 2, categoria: "350" },
            { id: 2, nombre: "Azithral 500 Tablet", codigo: "", cantidad: 2, categoria: "20" },
            { id: 3, nombre: "Ascoril LS Syrup", codigo: "D06ID232435452", cantidad: 2, categoria: "85" },
            { id: 4, nombre: "Azee 500 Tablet", codigo: "D06ID232435450", cantidad: 2, categoria: "75" },
            { id: 5, nombre: "Allegra 120mg Tablet", codigo: "D06ID232435455", cantidad: null, categoria: "44" },
            { id: 6, nombre: "Alex Syrup", codigo: "D06ID232435456", cantidad: null, categoria: "65" },
            { id: 7, nombre: "Amoxyclav 625 Tablet", codigo: "D06ID232435457", cantidad: 150, categoria: "150" },
            { id: 8, nombre: "Avil 25 Tablet", codigo: "D06ID232435458", cantidad: 270, categoria: "270" }
        ];

        this.renderTable(this.inventoryData);
        this.updateAlertCount(4);
    }

    escapeHtml(unsafe) {
        if (!unsafe) return '';
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
}

// Inicializar el manager cuando se carga la página
let inventoryManager;
document.addEventListener('DOMContentLoaded', () => {
    inventoryManager = new InventoryManager();
});