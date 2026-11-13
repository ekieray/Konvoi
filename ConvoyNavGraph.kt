package com.konvoiku.touring.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.konvoiku.touring.model.UserRole
import com.konvoiku.touring.ui.screens.AboutAppScreen
import com.konvoiku.touring.ui.screens.JoinTourScreen
import com.konvoiku.touring.ui.screens.LiveTrackingMapScreen
import com.konvoiku.touring.ui.screens.RoleSelectionScreen
import com.konvoiku.touring.ui.screens.TourLeaderScreen
import com.konvoiku.touring.ui.screens.TripDetailScreen
import com.konvoiku.touring.ui.screens.TripHistoryScreen
import com.konvoiku.touring.ui.screens.VehicleProfileScreen
import com.konvoiku.touring.viewmodel.ConvoyViewModel

sealed class ConvoyRoute(val route: String) {
    data object RoleSelection : ConvoyRoute("role_selection")
    data object VehicleProfile : ConvoyRoute("vehicle_profile")
    data object TourLeader : ConvoyRoute("tour_leader")
    data object JoinTour : ConvoyRoute("join_tour")
    data object LiveMap : ConvoyRoute("live_map")
    data object TripHistory : ConvoyRoute("trip_history")
    data object TripDetail : ConvoyRoute("trip_detail")
    data object AboutApp : ConvoyRoute("about_app")
}

@Composable
fun ConvoyNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: ConvoyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = ConvoyRoute.RoleSelection.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(260)
            ) + fadeIn(animationSpec = tween(220))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(260)
            ) + fadeOut(animationSpec = tween(180))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(260)
            ) + fadeIn(animationSpec = tween(220))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(260)
            ) + fadeOut(animationSpec = tween(180))
        }
    ) {
        composable(ConvoyRoute.RoleSelection.route) {
            RoleSelectionScreen(
                activeRoom = uiState.activeRoom,
                destinationName = uiState.routeConfig.destinationName,
                tripHistoryCount = uiState.tripHistory.size,
                latestTripTitle = uiState.tripHistory.firstOrNull()?.tourTitle,
                onRoleSelected = { role ->
                    viewModel.selectRole(role)
                    navController.navigate(ConvoyRoute.VehicleProfile.route)
                },
                onResumeCurrentConvoy = {
                    if (uiState.activeRoom == null) {
                        viewModel.selectRole(UserRole.TOUR_LEADER)
                        viewModel.createNewTourRoom()
                    }
                    navController.navigate(ConvoyRoute.LiveMap.route)
                },
                onOpenTripHistory = {
                    navController.navigate(ConvoyRoute.TripHistory.route)
                },
                onOpenAboutApp = {
                    navController.navigate(ConvoyRoute.AboutApp.route)
                }
            )
        }

        composable(ConvoyRoute.VehicleProfile.route) {
            val activeRole = uiState.selectedRole ?: UserRole.MEMBER
            VehicleProfileScreen(
                selectedRole = activeRole,
                profile = uiState.vehicleProfile,
                onUpdateProfile = { name, type, detail, plate, contact, preset, photoUri, clearPhoto ->
                    viewModel.updateVehicleProfile(
                        fullName = name,
                        vehicleType = type,
                        vehicleDetail = detail,
                        licensePlate = plate,
                        emergencyContact = contact,
                        avatarPreset = preset,
                        avatarPhotoUri = photoUri,
                        clearPhoto = clearPhoto
                    )
                },
                onBack = { navController.popBackStack() },
                onContinue = {
                    val destination = if (activeRole == UserRole.TOUR_LEADER) {
                        ConvoyRoute.TourLeader.route
                    } else {
                        ConvoyRoute.JoinTour.route
                    }
                    navController.navigate(destination)
                },
                onOpenAboutApp = {
                    navController.navigate(ConvoyRoute.AboutApp.route)
                }
            )
        }

        composable(ConvoyRoute.TourLeader.route) {
            TourLeaderScreen(
                activeRoom = uiState.activeRoom,
                routeConfig = uiState.routeConfig,
                destinationPresets = viewModel.destinationPresets,
                onSelectPreset = viewModel::selectDestinationPreset,
                onPickDestinationFromMap = { x, y, placeName, addressLabel ->
                    viewModel.setCustomDestinationPoint(x, y, placeName, addressLabel)
                },
                onUpdateWaypoints = viewModel::updateCustomWaypoints,
                onResetDefaultWaypoints = viewModel::resetDefaultWaypoints,
                onAddCheckpointAtPoint = viewModel::addRestCheckpointAtPoint,
                onRemoveCheckpoint = viewModel::removeRestCheckpoint,
                onUpdateMaxStragglerDistance = viewModel::updateMaxStragglerDistance,
                onToggleSoundAlert = viewModel::toggleSoundAlert,
                onCreateRoom = { title ->
                    viewModel.createNewTourRoom(title)
                    navController.navigate(ConvoyRoute.LiveMap.route)
                },
                onSimulateJoin = { viewModel.simulateMemberJoin() },
                onRemoveMember = viewModel::removeMember,
                onEnterMapPlaceholder = {
                    navController.navigate(ConvoyRoute.LiveMap.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(ConvoyRoute.JoinTour.route) {
            JoinTourScreen(
                joinInput = uiState.joinInput,
                joinError = uiState.joinError,
                extractedRoomId = viewModel.extractRoomId(uiState.joinInput),
                vehicleProfile = uiState.vehicleProfile,
                onInputChange = viewModel::updateJoinInput,
                onJoinSubmit = {
                    if (viewModel.joinConvoyRoom()) {
                        navController.navigate(ConvoyRoute.LiveMap.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(ConvoyRoute.LiveMap.route) {
            LiveTrackingMapScreen(
                activeRoom = uiState.activeRoom,
                role = uiState.selectedRole ?: UserRole.MEMBER,
                selectedMemberId = uiState.selectedMemberId,
                isSimulationRunning = uiState.isSimulationRunning,
                routeConfig = uiState.routeConfig,
                stragglerAlert = uiState.stragglerAlert,
                offRouteAlert = uiState.offRouteAlert,
                sosState = uiState.sosState,
                messages = uiState.messages,
                isRecordingVoice = uiState.isRecordingVoice,
                recordingSeconds = uiState.recordingSeconds,
                playingMessageId = uiState.playingMessageId,
                isPickingDestinationOnMap = uiState.isPickingDestinationOnMap,
                isPickingCheckpointOnMap = uiState.isPickingCheckpointOnMap,
                pendingCheckpointName = uiState.pendingCheckpointName,
                isOffRouteSimActive = uiState.isOffRouteSimActive,
                isStragglerSimActive = uiState.isStragglerSimActive,
                destinationPresets = viewModel.destinationPresets,
                onSelectMember = viewModel::selectMapMember,
                onToggleSimulation = viewModel::toggleSimulation,
                onSelectPreset = viewModel::selectDestinationPreset,
                onPickCustomDestination = { x, y ->
                    viewModel.setCustomDestinationPoint(x, y)
                },
                onTogglePickMode = viewModel::setPickingDestinationMode,
                onStartPickCheckpoint = viewModel::startPickingCheckpointOnMap,
                onCancelPickCheckpoint = viewModel::cancelPickingCheckpoint,
                onConfirmAddCheckpoint = viewModel::addRestCheckpointAtPoint,
                onRemoveCheckpoint = viewModel::removeRestCheckpoint,
                onUpdateMaxStragglerDistance = viewModel::updateMaxStragglerDistance,
                onUpdateOffRouteTolerance = viewModel::updateOffRouteTolerance,
                onToggleSoundAlert = viewModel::toggleSoundAlert,
                onToggleOffRouteSim = viewModel::toggleOffRouteSimulation,
                onToggleStragglerSim = viewModel::toggleStragglerSimulation,
                onTriggerSos = viewModel::triggerSosEmergency,
                onResolveSos = viewModel::resolveSosEmergency,
                onSendTextMessage = viewModel::sendTextMessage,
                onStartVoiceRecord = viewModel::startVoiceRecording,
                onStopVoiceRecord = { viewModel.stopAndSendVoiceRecording() },
                onToggleVoicePlay = viewModel::toggleVoicePlayback,
                onUpdateOwnCondition = viewModel::updateOwnConditionStatus,
                onAssignSweeper = viewModel::assignSweeper,
                onFinishAndSaveTrip = {
                    val saved = viewModel.finishAndSaveCurrentTrip()
                    if (saved != null) {
                        navController.navigate(ConvoyRoute.TripDetail.route) {
                            popUpTo(ConvoyRoute.RoleSelection.route)
                        }
                    }
                },
                onExitRoom = {
                    navController.popBackStack(
                        route = ConvoyRoute.RoleSelection.route,
                        inclusive = false
                    )
                }
            )
        }

        composable(ConvoyRoute.TripHistory.route) {
            TripHistoryScreen(
                trips = uiState.tripHistory,
                onSelectTrip = { tripId ->
                    viewModel.selectTripHistory(tripId)
                    navController.navigate(ConvoyRoute.TripDetail.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(ConvoyRoute.TripDetail.route) {
            val selectedTrip = uiState.tripHistory.find { it.id == uiState.selectedTripId }
                ?: uiState.tripHistory.firstOrNull()
            TripDetailScreen(
                trip = selectedTrip,
                onBack = { navController.popBackStack() }
            )
        }

        composable(ConvoyRoute.AboutApp.route) {
            AboutAppScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
