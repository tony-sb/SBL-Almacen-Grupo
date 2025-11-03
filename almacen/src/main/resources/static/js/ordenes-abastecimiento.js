// ordenes-compra.js - VERSIÓN CORREGIDA
console.log("=== INICIANDO SISTEMA DE ÓRDENES DE COMPRA ===");

// Variable global para precios de productos
window.preciosProductos = {};

document.addEventListener('DOMContentLoaded', function() {
    console.log("✅ DOM cargado - Inicializando sistema de órdenes");

    // 1. Inicializar precios de productos
    inicializarPreciosProductos();

    // 2. Configurar según el tipo de página
    if (document.getElementById('formOrden')) {
        console.log("🔄 Formulario detectado - inicializando...");
        inicializarFormularioCompleto();
    }

    // 3. Configurar confirmación de eliminación
    configurarConfirmacionEliminar();

    // 4. Configurar tooltips
    configurarTooltips();

    console.log("✅ Sistema de órdenes inicializado completamente");
});

function inicializarPreciosProductos() {
    console.log("💰 Inicializando precios de productos...");
    if (window.preciosProductos && Object.keys(window.preciosProductos).length > 0) {
        console.log(`✅ ${Object.keys(window.preciosProductos).length} precios cargados`);
    } else {
        console.warn("⚠️ No se encontraron precios de productos");
        window.preciosProductos = {};
    }
}

function inicializarFormularioCompleto() {
    console.log("📝 Inicializando formulario completo...");

    // Configurar botón agregar producto
    const addBtn = document.getElementById('add-item');
    if (addBtn) {
        addBtn.onclick = function() {
            agregarNuevoItem();
        };
        console.log("✅ Botón agregar producto configurado");
    } else {
        console.error("❌ Botón agregar producto NO encontrado");
    }

    // Configurar validación del formulario
    const form = document.getElementById('formOrden');
    if (form) {
        form.onsubmit = validarFormularioCompleto;
        console.log("✅ Validación de formulario configurada");
    }

    // Inicializar items existentes
    inicializarItems();

    console.log("✅ Formulario completo inicializado");
}

function inicializarItems() {
    console.log("🔄 Inicializando items...");
    const items = document.querySelectorAll('.item-row');
    console.log(`📦 Encontrados ${items.length} items`);

    items.forEach((item, index) => {
        console.log(`   🔧 Inicializando item ${index + 1}`);
        configurarEventosItem(item);
        calcularSubtotal(item);
    });

    calcularTotal();
    actualizarContadorItems();
}

function configurarEventosItem(item) {
    const productoSelect = item.querySelector('.producto-select');
    const cantidadInput = item.querySelector('.cantidad-input');
    const precioInput = item.querySelector('.precio-input');
    const removeBtn = item.querySelector('.remove-item');

    if (!productoSelect || !cantidadInput || !precioInput || !removeBtn) {
        console.error("❌ Elementos faltantes en item:", item);
        return;
    }

    // Configurar eventos del select de producto
    productoSelect.onchange = function() {
        const productoId = this.value;
        console.log(`🔄 Producto cambiado: ${productoId}`);

        if (productoId && window.preciosProductos[productoId]) {
            precioInput.value = window.preciosProductos[productoId];
            console.log(`💰 Precio automático asignado: ${window.preciosProductos[productoId]}`);
            mostrarFeedbackPrecio(precioInput, 'auto');
        } else if (productoId) {
            // Producto sin precio definido, mantener valor actual o 0
            if (!precioInput.value || precioInput.value === '0.00') {
                precioInput.value = '0.00';
            }
            mostrarFeedbackPrecio(precioInput, 'manual');
        } else {
            // Producto deseleccionado
            precioInput.value = '0.00';
        }

        calcularSubtotal(item);
        calcularTotal();
        validarItemCompleto(item);
    };

    // Configurar eventos de cantidad
    cantidadInput.oninput = function() {
        if (this.value < 1) this.value = 1;
        if (this.value > 9999) this.value = 9999;
        calcularSubtotal(item);
        calcularTotal();
        validarItemCompleto(item);
    };

    cantidadInput.onblur = function() {
        if (!this.value || this.value < 1) {
            this.value = 1;
            calcularSubtotal(item);
            calcularTotal();
        }
    };

    // Configurar eventos de precio
    precioInput.oninput = function() {
        if (this.value < 0) this.value = 0;
        if (this.value > 999999) this.value = 999999;
        calcularSubtotal(item);
        calcularTotal();
        mostrarFeedbackPrecio(this, 'manual');
        validarItemCompleto(item);
    };

    precioInput.onblur = function() {
        if (!this.value || this.value < 0) {
            this.value = '0.00';
            calcularSubtotal(item);
            calcularTotal();
        }
    };

    // Configurar botón eliminar
    removeBtn.onclick = function() {
        console.log("🗑️ Intentando eliminar item...");
        eliminarItem(item);
    };

    // Configurar validación inicial
    validarItemCompleto(item);

    console.log("✅ Eventos de item configurados correctamente");
}

function agregarNuevoItem() {
    console.log(`➕ Agregando nuevo item...`);
    const container = document.getElementById('items-container');

    if (!container) {
        console.error("❌ Contenedor de items no encontrado");
        mostrarAlerta('error', 'Error: No se puede agregar items. Recarga la página.');
        return;
    }

    const items = container.querySelectorAll('.item-row');

    if (items.length >= 50) {
        console.warn("⚠️ Límite de items alcanzado");
        mostrarAlerta('warning', 'Máximo 50 productos por orden');
        return;
    }

    // Buscar el primer item para clonar (siempre existe)
    const primerItem = items[0];
    const nuevoItem = primerItem.cloneNode(true);

    // Limpiar valores del nuevo item
    resetearItem(nuevoItem);

    // Insertar antes del botón agregar
    container.appendChild(nuevoItem);

    // Configurar eventos del nuevo item
    configurarEventosItem(nuevoItem);
    calcularTotal();
    actualizarContadorItems();

    console.log("✅ Nuevo item agregado correctamente");

    // Scroll y focus al nuevo item
    setTimeout(() => {
        nuevoItem.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        const nuevoSelect = nuevoItem.querySelector('.producto-select');
        if (nuevoSelect) nuevoSelect.focus();
    }, 100);
}

function eliminarItem(item) {
    const items = document.querySelectorAll('.item-row');
    console.log(`📦 Total de items: ${items.length}`);

    if (items.length > 1) {
        // Animación de eliminación
        item.style.opacity = '0.5';
        item.style.backgroundColor = '#ffe6e6';

        setTimeout(() => {
            item.remove();
            calcularTotal();
            actualizarContadorItems();
            console.log("✅ Item eliminado");
        }, 300);
    } else {
        console.log("📝 Último item - reseteando valores...");
        resetearItem(item);
    }
}

function resetearItem(item) {
    const productoSelect = item.querySelector('.producto-select');
    const cantidadInput = item.querySelector('.cantidad-input');
    const precioInput = item.querySelector('.precio-input');
    const subtotalDisplay = item.querySelector('.subtotal-display');

    if (productoSelect) productoSelect.selectedIndex = 0;
    if (cantidadInput) cantidadInput.value = '1';
    if (precioInput) precioInput.value = '0.00';
    if (subtotalDisplay) subtotalDisplay.value = 'S/ 0.00';

    // Resetear estilos
    item.style.borderColor = '';
    item.style.backgroundColor = '';
    item.style.opacity = '';

    calcularSubtotal(item);
}

function calcularSubtotal(item) {
    const cantidadInput = item.querySelector('.cantidad-input');
    const precioInput = item.querySelector('.precio-input');
    const subtotalDisplay = item.querySelector('.subtotal-display');

    if (!cantidadInput || !precioInput || !subtotalDisplay) return;

    const cantidad = parseFloat(cantidadInput.value) || 0;
    const precio = parseFloat(precioInput.value) || 0;
    const subtotal = cantidad * precio;

    subtotalDisplay.value = 'S/ ' + subtotal.toFixed(2);
}

function calcularTotal() {
    let total = 0;
    let itemsValidos = 0;
    const items = document.querySelectorAll('.item-row');

    items.forEach(item => {
        const cantidadInput = item.querySelector('.cantidad-input');
        const precioInput = item.querySelector('.precio-input');

        if (cantidadInput && precioInput) {
            const cantidad = parseFloat(cantidadInput.value) || 0;
            const precio = parseFloat(precioInput.value) || 0;
            const subtotal = cantidad * precio;

            total += subtotal;

            if (cantidad > 0 && precio > 0) {
                itemsValidos++;
            }
        }
    });

    // Actualizar total en formulario
    const formTotal = document.getElementById('total-orden');
    if (formTotal) {
        formTotal.textContent = 'S/ ' + total.toFixed(2);
    }

    // Actualizar total en modal
    const modalTotal = document.getElementById('total-modal');
    if (modalTotal) {
        modalTotal.textContent = 'S/ ' + total.toFixed(2);
    }

    // Actualizar contador de items válidos
    const contadorItems = document.getElementById('contador-items');
    if (contadorItems) {
        contadorItems.textContent = `${itemsValidos} producto(s) válido(s)`;
        contadorItems.className = itemsValidos > 0 ? 'badge bg-success' : 'badge bg-secondary';
    }

    // Actualizar contador total de items
    actualizarContadorItems();

    return total;
}

function actualizarContadorItems() {
    const items = document.querySelectorAll('.item-row');
    const contador = document.getElementById('contador-items-total');

    if (contador) {
        contador.textContent = `${items.length} producto(s) en la orden`;
    }
}

function validarItemCompleto(item) {
    const productoSelect = item.querySelector('.producto-select');
    const cantidadInput = item.querySelector('.cantidad-input');
    const precioInput = item.querySelector('.precio-input');

    const productoValido = productoSelect && productoSelect.value !== "";
    const cantidadValida = cantidadInput && cantidadInput.value > 0;
    const precioValido = precioInput && precioInput.value >= 0;

    const esValido = productoValido && cantidadValida && precioValido;

    // Feedback visual
    if (esValido) {
        item.style.borderColor = '#198754';
        item.style.backgroundColor = '#f8fff9';
    } else {
        item.style.borderColor = '#dee2e6';
        item.style.backgroundColor = '#f8f9fa';
    }

    return esValido;
}

function mostrarFeedbackPrecio(input, tipo) {
    if (tipo === 'auto') {
        input.style.borderColor = '#198754';
        input.title = 'Precio cargado automáticamente';
    } else {
        input.style.borderColor = '#fd7e14';
        input.title = 'Precio ingresado manualmente';
    }
}

// Resto de funciones permanecen igual...
function configurarConfirmacionEliminar() {
    console.log("🗑️ Configurando confirmaciones de eliminación...");
    document.addEventListener('click', function(e) {
        if (e.target.closest('.btn-eliminar')) {
            e.preventDefault();
            const target = e.target.closest('.btn-eliminar');
            const confirmar = confirm('¿Está seguro de eliminar esta orden?');
            if (confirmar) {
                window.location.href = target.href;
            }
        }
    });
}

function configurarTooltips() {
    const tooltips = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltips.forEach(el => new bootstrap.Tooltip(el));
}

function mostrarAlerta(tipo, mensaje) {
    // Implementación simple de alerta
    alert(mensaje);
}

window.ordenesApp = {
    inicializarItems,
    agregarNuevoItem,
    eliminarItem,
    calcularTotal,
    debugItems: function() {
        const items = document.querySelectorAll('.item-row');
        console.log(`=== DEBUG ITEMS (${items.length} total) ===`);
        items.forEach((item, index) => {
            const productoSelect = item.querySelector('.producto-select');
            console.log(`Item ${index + 1}:`, {
                producto: productoSelect?.value,
                productoNombre: productoSelect?.options[productoSelect.selectedIndex]?.text,
                valido: validarItemCompleto(item)
            });
        });
    }
};