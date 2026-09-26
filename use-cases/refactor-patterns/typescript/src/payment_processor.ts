// Payment processing system that needs to work with different payment gateways

// Our application's payment processor interface
interface PaymentProcessor {
  processPayment(amount: number, cardDetails: CreditCardDetails): Promise<PaymentResult>;
  refundPayment(transactionId: string, amount: number): Promise<RefundResult>;
}

// Our application's data types
interface CreditCardDetails {
  cardNumber: string;
  expiryMonth: number;
  expiryYear: number;
  cvv: string;
  cardholderName: string;
}

interface PaymentResult {
  success: boolean;
  transactionId?: string;
  errorMessage?: string;
}

interface RefundResult {
  success: boolean;
  refundId?: string;
  errorMessage?: string;
}

// First external payment gateway API (Stripe-like)
class StripeAPI {
  async createCharge(
    amountInCents: number,
    source: {
      cc_number: string,
      exp_month: number,
      exp_year: number,
      cvc: string,
      name: string
    },
    currency: string = 'usd'
  ): Promise<{ id: string, status: string, error?: { message: string } }> {
    // This would call the actual Stripe API in a real implementation
    console.log(`Stripe: Charging ${amountInCents / 100} ${currency.toUpperCase()}`);

    // Simulate API call
    if (source.cc_number === '4111111111111111') {
      return {
        id: `stripe_tx_${Date.now()}`,
        status: 'succeeded'
      };
    } else {
      return {
        id: '',
        status: 'failed',
        error: { message: 'Invalid card number' }
      };
    }
  }

  async createRefund(
    chargeId: string,
    amountInCents: number
  ): Promise<{ id: string, status: string, error?: { message: string } }> {
    console.log(`Stripe: Refunding charge ${chargeId}`);

    // Simulate API call
    if (chargeId.startsWith('stripe_tx_')) {
      return {
        id: `stripe_re_${Date.now()}`,
        status: 'succeeded'
      };
    } else {
      return {
        id: '',
        status: 'failed',
        error: { message: 'Invalid charge ID' }
      };
    }
  }
}

// Second external payment gateway API (PayPal-like)
class PayPalAPI {
  async submitPayment(
    payment: {
      amount: number,
      card: {
        number: string,
        expiration: string, // "MM/YY" format
        securityCode: string,
        holder: string
      }
    }
  ): Promise<{ paymentId: string, successful: boolean, failureReason?: string }> {
    // This would call the actual PayPal API in a real implementation
    console.log(`PayPal: Processing payment of $${payment.amount}`);

    // Simulate API call
    if (payment.card.number === '4111111111111111') {
      return {
        paymentId: `paypal_pmt_${Date.now()}`,
        successful: true
      };
    } else {
      return {
        paymentId: '',
        successful: false,
        failureReason: 'Card declined'
      };
    }
  }

  async refund(
    paymentId: string,
    refundAmount: number
  ): Promise<{ refundId: string, successful: boolean, failureReason?: string }> {
    console.log(`PayPal: Refunding payment ${paymentId}`);

    // Simulate API call
    if (paymentId.startsWith('paypal_pmt_')) {
      return {
        refundId: `paypal_ref_${Date.now()}`,
        successful: true
      };
    } else {
      return {
        refundId: '',
        successful: false,
        failureReason: 'Payment not found'
      };
    }
  }
}

// Direct usage without adapters (hard to maintain as requirements change)
class PaymentService {
  private stripeAPI: StripeAPI;
  private paypalAPI: PayPalAPI;
  private preferredGateway: 'stripe' | 'paypal';

  constructor(preferredGateway: 'stripe' | 'paypal' = 'stripe') {
    this.stripeAPI = new StripeAPI();
    this.paypalAPI = new PayPalAPI();
    this.preferredGateway = preferredGateway;
  }

  async processPayment(amount: number, cardDetails: CreditCardDetails): Promise<PaymentResult> {
    try {
      if (this.preferredGateway === 'stripe') {
        // Use Stripe API
        const stripeResult = await this.stripeAPI.createCharge(
          amount * 100, // Convert to cents
          {
            cc_number: cardDetails.cardNumber,
            exp_month: cardDetails.expiryMonth,
            exp_year: cardDetails.expiryYear,
            cvc: cardDetails.cvv,
            name: cardDetails.cardholderName
          }
        );

        if (stripeResult.status === 'succeeded') {
          return {
            success: true,
            transactionId: stripeResult.id
          };
        } else {
          return {
            success: false,
            errorMessage: stripeResult.error?.message || 'Unknown error'
          };
        }
      } else {
        // Use PayPal API
        const formattedExpiry = `${String(cardDetails.expiryMonth).padStart(2, '0')}/${String(cardDetails.expiryYear).slice(-2)}`;

        const paypalResult = await this.paypalAPI.submitPayment({
          amount: amount,
          card: {
            number: cardDetails.cardNumber,
            expiration: formattedExpiry,
            securityCode: cardDetails.cvv,
            holder: cardDetails.cardholderName
          }
        });

        if (paypalResult.successful) {
          return {
            success: true,
            transactionId: paypalResult.paymentId
          };
        } else {
          return {
            success: false,
            errorMessage: paypalResult.failureReason || 'Unknown error'
          };
        }
      }
    } catch (error) {
      return {
        success: false,
        errorMessage: `Payment processing error: ${error.message}`
      };
    }
  }

  async refundPayment(transactionId: string, amount: number): Promise<RefundResult> {
    try {
      if (transactionId.startsWith('stripe_tx_')) {
        // Use Stripe API for refunds
        const refundResult = await this.stripeAPI.createRefund(
          transactionId,
          amount * 100 // Convert to cents
        );

        if (refundResult.status === 'succeeded') {
          return {
            success: true,
            refundId: refundResult.id
          };
        } else {
          return {
            success: false,
            errorMessage: refundResult.error?.message || 'Unknown error'
          };
        }
      } else if (transactionId.startsWith('paypal_pmt_')) {
        // Use PayPal API for refunds
        const refundResult = await this.paypalAPI.refund(
          transactionId,
          amount
        );

        if (refundResult.successful) {
          return {
            success: true,
            refundId: refundResult.refundId
          };
        } else {
          return {
            success: false,
            errorMessage: refundResult.failureReason || 'Unknown error'
          };
        }
      } else {
        return {
          success: false,
          errorMessage: 'Unknown transaction ID format'
        };
      }
    } catch (error) {
      return {
        success: false,
        errorMessage: `Refund processing error: ${error.message}`
      };
    }
  }
}

// Example usage
async function runExample() {
  const paymentService = new PaymentService('stripe');

  const cardDetails: CreditCardDetails = {
    cardNumber: '4111111111111111',
    expiryMonth: 12,
    expiryYear: 2025,
    cvv: '123',
    cardholderName: 'John Doe'
  };

  // Process a payment
  const paymentResult = await paymentService.processPayment(99.99, cardDetails);
  console.log('Payment result:', paymentResult);

  // Process a refund if payment was successful
  if (paymentResult.success && paymentResult.transactionId) {
    const refundResult = await paymentService.refundPayment(paymentResult.transactionId, 99.99);
    console.log('Refund result:', refundResult);
  }
}

runExample();