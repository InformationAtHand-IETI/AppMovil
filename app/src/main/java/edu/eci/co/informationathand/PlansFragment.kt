package edu.eci.co.informationathand

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class PlansFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_plans, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Plan Básico (Gratis)
        view.findViewById<CardView>(R.id.btnBasicPlan).setOnClickListener {
            showPlanDialog(
                title = "Plan Básico",
                price = "Gratis",
                features = listOf(
                    "Alertas de zonas peligrosas",
                    "Mapas básicos de seguridad",
                    "Reportes ciudadanos simples"
                ),
                buttonText = "Comenzar Gratis",
                planType = "basic"
            )
        }

        view.findViewById<Button>(R.id.btnSelectBasic).setOnClickListener {
            selectPlan("Básico", "Gratis")
        }

        // Plan Premium Individual
        view.findViewById<CardView>(R.id.btnPremiumPlan).setOnClickListener {
            showPlanDialog(
                title = "Premium Individual",
                price = "$15.000/mes",
                features = listOf(
                    "Todo del plan básico",
                    "Alertas en tiempo real",
                    "Predicciones de riesgo con IA",
                    "Estadísticas personalizadas",
                    "Notificaciones push prioritarias"
                ),
                buttonText = "Suscribirme",
                planType = "premium"
            )
        }

        view.findViewById<Button>(R.id.btnSelectPremium).setOnClickListener {
            selectPlan("Premium Individual", "$15.000/mes")
        }

        // Plan Premium Familiar
        view.findViewById<CardView>(R.id.btnFamilyPlan).setOnClickListener {
            showPlanDialog(
                title = "Premium Familiar",
                price = "$40.000/mes",
                features = listOf(
                    "Todo del plan individual",
                    "Hasta 6 perfiles familiares",
                    "Cobertura extendida",
                    "Alertas geolocalizadas por persona",
                    "Panel de control familiar"
                ),
                buttonText = "Suscribirme",
                planType = "family"
            )
        }

        view.findViewById<Button>(R.id.btnSelectFamily).setOnClickListener {
            selectPlan("Premium Familiar", "$40.000/mes")
        }
    }

    private fun showPlanDialog(
        title: String,
        price: String,
        features: List<String>,
        buttonText: String,
        planType: String
    ) {
        val message = buildString {
            append("💰 Precio: $price\n\n")
            append("📋 Características:\n")
            features.forEach { feature ->
                append("✓ $feature\n")
            }
            append("\n")
            when (planType) {
                "basic" -> append("¡Perfecto para comenzar!")
                "premium" -> append("¡El plan más elegido por nuestros usuarios!")
                "family" -> append("¡Protege a toda tu familia!")
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("🎯 $title")
            .setMessage(message)
            .setPositiveButton(buttonText) { dialog, _ ->
                selectPlan(title, price)
                dialog.dismiss()
            }
            .setNegativeButton("Comparar Planes") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun selectPlan(planName: String, price: String) {
        // Aquí integrarías con tu sistema de pagos (Stripe, PayU, etc.)
        val message = if (price == "Gratis") {
            "✅ ¡Ya estás usando el $planName!"
        } else {
            "🔄 Procesando suscripción al $planName ($price)\n\nEn producción, aquí se abriría el portal de pagos."
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (price == "Gratis") "Plan Activado" else "Suscripción Pendiente")
            .setMessage(message)
            .setPositiveButton("Entendido") { dialog, _ ->
                Toast.makeText(
                    requireContext(),
                    "Plan $planName seleccionado",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            .setNeutralButton("Ver Beneficios") { dialog, _ ->
                showBenefitsDialog(planName)
                dialog.dismiss()
            }
            .show()
    }

    private fun showBenefitsDialog(planName: String) {
        val benefits = when (planName) {
            "Básico" -> """
                🆓 Plan Gratuito
                
                • Acceso a mapas de seguridad
                • Ver reportes de otros usuarios
                • Crear reportes básicos
                • Alertas de zonas de alto riesgo
                
                Ideal para uso casual
            """.trimIndent()

            "Premium Individual" -> """
                ⭐ Plan Premium
                
                • Todo lo del plan básico
                • Alertas en tiempo real
                • Predicción de riesgos con IA
                • Estadísticas detalladas
                • Notificaciones prioritarias
                • Sin publicidad
                • Historial de 90 días
                
                Perfecto para usuarios frecuentes
            """.trimIndent()

            else -> """
                👨‍👩‍👧‍👦 Plan Familiar
                
                • Todo lo del plan premium
                • Hasta 6 perfiles familiares
                • Seguimiento individual
                • Alertas por cada miembro
                • Panel de control familiar
                • Cobertura nacional
                • Soporte prioritario 24/7
                
                La mejor opción para familias
            """.trimIndent()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Beneficios - $planName")
            .setMessage(benefits)
            .setPositiveButton("Cerrar", null)
            .show()
    }
}