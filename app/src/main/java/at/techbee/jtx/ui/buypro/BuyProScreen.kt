/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.buypro

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.R
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import at.techbee.jtx.ui.reusable.cards.BuyProCard
import at.techbee.jtx.ui.reusable.cards.BuyProCardPurchased
import at.techbee.jtx.ui.theme.Typography


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyProScreen(
    isPurchased: State<Boolean?>,
    priceLive: LiveData<String?>,
    purchaseDateLive: LiveData<String?>,
    orderIdLive: LiveData<String?>,
    launchBillingFlow: () -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(id = R.string.navigation_drawer_buypro)
            )
        },
        content = { paddingValues ->
            JtxNavigationDrawer(
                drawerState = drawerState,
                mainContent = {

                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {


                        Image(
                            painter = painterResource(id = R.drawable.bg_adfree),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(top = 32.dp, bottom = 32.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.buypro_text),
                            modifier = Modifier.padding(top = 16.dp),
                            style = Typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )

                        Crossfade(targetState = isPurchased.value) {

                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                if (it == false) {
                                    BuyProCard(
                                        priceLive = priceLive,
                                        onClick = { launchBillingFlow() }
                                    )
                                } else if(it == true) {
                                    BuyProCardPurchased(
                                        purchaseDateLive = purchaseDateLive,
                                        orderIdLive = orderIdLive
                                    )
                                    Text(
                                        text = stringResource(id = R.string.buypro_success_thankyou),
                                        modifier = Modifier
                                            .padding(top = 16.dp)
                                            .fillMaxWidth(),
                                        style = Typography.displaySmall,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Image(
                                        painter = painterResource(id = R.drawable.bg_thankyou),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(240.dp)
                                            .padding(top = 32.dp, bottom = 32.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                navController = navController,
                paddingValues = paddingValues
            )

        }
    )

}

@Preview(showBackground = true)
@Composable
fun BuyProScreen_Preview() {
    MaterialTheme {
        BuyProScreen(
            isPurchased = remember { mutableStateOf(false) },
            priceLive = MutableLiveData("€ 3,29"),
            orderIdLive = MutableLiveData("93287z4"),
            purchaseDateLive = MutableLiveData("Thursday, 1. Jannuary 2021"),
            launchBillingFlow = { },
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BuyProScreen_Preview_purchased() {
    MaterialTheme {
        BuyProScreen(
            isPurchased = remember { mutableStateOf(true) },
            priceLive = MutableLiveData("€ 3,29"),
            orderIdLive = MutableLiveData("93287z4"),
            purchaseDateLive = MutableLiveData("Thursday, 1. Jannuary 2021"),
            launchBillingFlow = { },
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BuyProScreen_Preview_null() {
    MaterialTheme {
        BuyProScreen(
            isPurchased = remember { mutableStateOf(null) },
            priceLive = MutableLiveData("€ 3,29"),
            orderIdLive = MutableLiveData("93287z4"),
            purchaseDateLive = MutableLiveData("Thursday, 1. Jannuary 2021"),
            launchBillingFlow = { },
            navController = rememberNavController()
        )
    }
}