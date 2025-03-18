const express = require("express");
const stripe = require("stripe")("sk_test_xxx"); // Replace with your Stripe Secret Key
const app = express();
const cors = require("cors");

app.use(cors());
app.use(express.json());

app.post("/create-payment-intent", async (req, res) => {
    try {
        const { amount } = req.body;

        const paymentIntent = await stripe.paymentIntents.create({
            amount: amount,
            currency: "INR",
            automatic_payment_methods: { enabled: true },
        });

        res.json({ clientSecret: paymentIntent.client_secret });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

app.listen(3000, () => console.log("Server running on port 3000"));
